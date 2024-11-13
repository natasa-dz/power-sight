import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { UserService } from "../service/user.service";

@Component({
  selector: 'app-change-password-dto.model.ts',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  changePasswordForm: FormGroup;
  currentUser: any;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    public dialogRef: MatDialogRef<ChangePasswordComponent>
  ) {
    // Custom Validator for matching passwords
    this.changePasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordsMatchValidator });
  }

  // Custom validator to check if passwords match
  passwordsMatchValidator(formGroup: FormGroup) {
    const newPassword = formGroup.get('newPassword')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;

    // If the newPassword and confirmPassword don't match, return an error
    return newPassword && confirmPassword && newPassword !== confirmPassword
      ? { passwordsDoNotMatch: true }
      : null;
  }

  changePassword() {
    if (this.changePasswordForm.valid) {
      const { newPassword } = this.changePasswordForm.value;
      const {confirmPassword} = this.changePasswordForm.value;

      this.userService.getCurrentUser().subscribe((user: any) => {
        this.currentUser = user;

        // Use the correct properties directly
        const username = this.currentUser.username;

        // Create DTO object to send to the backend
        const changePasswordDto = {
          username,
          confirmPassword,
          newPassword
        };

        console.log("ChangePassword DTO: ", changePasswordDto);

        // Call the changePassword API
        this.userService.changePassword(changePasswordDto).subscribe({
          next: (response) => {
            if (response.status === 200) {
              alert('Password changed successfully');
              this.dialogRef.close(true);  // Close dialog and return success
            }
            else {
              alert('Failed to change password: ' + response.body);
            }
          },
          error: (error) => {
            console.error('Error changing password: ', error);
            alert('Failed to change password');
          }
        });
      });
    }
  }

  cancel() {
    this.dialogRef.close(false);  // Close dialog without success
  }
}
