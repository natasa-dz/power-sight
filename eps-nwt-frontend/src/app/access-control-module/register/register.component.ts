import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {AuthService} from "../auth.service";
import {UserService} from "../../service/user.service";
import {Role} from "../../model/user.model";
import {ImageCompress} from "ngx-image-compress/lib/image-compress";
import {NgxImageCompressService} from "ngx-image-compress";
@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  registerForm: FormGroup;
  selectedFile: File | null = null;
  profilePic: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private imageCompress: NgxImageCompressService,

  ) {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      profilePic: [null]
    });
  }

  base64ToFile(base64String: string, fileName: string): File {
    const arr = base64String.split(',');
    const mime = arr[0].match(/:(.*?);/)![1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    return new File([u8arr], fileName, { type: mime });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.imageCompress.compressFile(URL.createObjectURL(file), -1, 50, 50).then(
        (compressedImage) => {
          this.profilePic = compressedImage;
          this.selectedFile = this.base64ToFile(compressedImage, file.name); // Convert base64 back to File        }
        });
    }
  }



  register(): void {
    console.log(this.registerForm.get('username')?.value);
    console.log(this.registerForm.get('password')?.value);

    if (this.registerForm.valid && this.selectedFile != null) {
      const formData = new FormData();
      let role = 'CITIZEN';

      if (this.authService.isLoggedIn() && this.authService.getRole() === Role.SUPERADMIN) {
        role = 'ADMIN';
      }
      formData.append('role', role)

      formData.append('username', this.registerForm.get('username')?.value);
      formData.append('password', this.registerForm.get('password')?.value);
      formData.append('userPhoto', this.selectedFile);
      this.userService.registerUser(formData).subscribe({
        next: (response) => {
          alert('Registration successful! Please check your email to activate your account.');
          this.authService.logout();
          this.router.navigate(['/login']);
        },
        error: () => {
          alert('Registration failed. Please try again.');
        }
      });
    } else {
      alert('Please fill out all required fields with valid information.');
    }
  }
}
