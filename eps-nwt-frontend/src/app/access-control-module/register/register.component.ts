import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {AuthService} from "../auth.service";
import {UserService} from "../../service/user.service";
import {Role} from "../../model/user.model";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  selectedFile: File | null = null;
  profilePicBase64: string | null = null;
  showActivationPrompt: boolean = false;
  activationToken: string = '';




  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      profilePic: [null]
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.registerForm.patchValue({ profilePic: file });
    }
  }

  private convertToBase64(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      this.profilePicBase64 = reader.result as string;  // Store base64 string
    };
    reader.readAsDataURL(file);
  }



  register(): void {

    if (this.registerForm.valid && this.profilePicBase64!=null) {
      const user = {
        username:this.registerForm.get('username')?.value,
        email:this.registerForm.get('email')?.value,
        password:this.registerForm.get('password')?.value,
        isActive: false,
        activationToken:'',
        passwordChanged:true,
        role: Role.CITIZEN,
        userPhoto: this.profilePicBase64// Include base64 profile picture
      }


      this.userService.registerUser(user).subscribe({
        next: (response) => {
          alert('Registration successful! Please check your email to activate your account.');
          this.showActivationPrompt = true; // Show the token input form
        },
        error: () => {
          alert('Registration failed. Please try again.');
        }
      });
    } else {
      alert('Please fill out all required fields with valid information.');
    }
  }

  isFormValid(): boolean {
    // Add validation logic for user registration form
    return true;
  }

  activateAccount(){
    this.userService.activateAccount(this.activationToken).subscribe({
      next:(response)=>{
        alert(response);
        this.router.navigate(['login']);
      },
      error:()=>{
        alert('Activation failed. Please check your token and try again! ')
      }
    });
  }

}
