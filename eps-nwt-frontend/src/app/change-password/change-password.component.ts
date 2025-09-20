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
    this.changePasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordsMatchValidator });
  }

  passwordsMatchValidator(formGroup: FormGroup) {
    const newPassword = formGroup.get('newPassword')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;

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

        const username = this.currentUser.username;

        const changePasswordDto = {
          username,
          confirmPassword,
          newPassword
        };


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
