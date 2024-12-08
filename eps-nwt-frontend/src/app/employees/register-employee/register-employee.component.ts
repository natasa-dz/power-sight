import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {AuthService} from "../../access-control-module/auth.service";
import {UserService} from "../../service/user.service";
import {NgxImageCompressService} from "ngx-image-compress";
import {Role} from "../../model/user.model";
import {BaseModule} from "../../base/base.module";

@Component({
  selector: 'app-register-employee',
  standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        RouterLink,
        BaseModule
    ],
  templateUrl: './register-employee.component.html',
  styleUrl: './register-employee.component.css'
})
export class RegisterEmployeeComponent {
  registerForm: FormGroup;
  selectedFile: File | null = null;
  profilePic: string | null = null;
  showActivationPrompt: boolean = false;
  activationToken: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private imageCompress: NgxImageCompressService,
    private route:ActivatedRoute,
  ) {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      jmbg: ['', [Validators.required, Validators.minLength(13), Validators.maxLength(13)]],
      name: ['', Validators.required],
      surname: ['', Validators.required],
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
    console.log(this.registerForm.get('jmbg')?.value);

    // Check if the form is valid and the file is selected
    if (this.registerForm.valid && this.selectedFile != null) {
      const formData = new FormData();
      let role = 'EMPLOYEE';

      formData.append('role', role)

      formData.append('username', this.registerForm.get('username')?.value);
      formData.append('password', this.registerForm.get('jmbg')?.value);
      formData.append('userPhoto', this.selectedFile);
      formData.append('userData', `{"name": "${this.registerForm.get('name')?.value}","surname": "${this.registerForm.get('surname')?.value}"}`);
      console.log(formData)
      this.userService.registerUser(formData).subscribe({
        next: (response) => {
          alert('Employee is registered!');
          this.registerForm.reset();
          this.selectedFile = null;
          this.profilePic = null;
        },
        error: () => {
        }
      });
    } else {
      alert('Please fill out all required fields with valid information.');
    }
  }
}
