// change-password-dto.model.ts.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import {UserService} from "../service/user.service";

@Component({
  selector: 'app-change-password-dto.model.ts',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  changePasswordForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    public dialogRef: MatDialogRef<ChangePasswordComponent>
  ) {
    this.changePasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    });
  }

  changePassword() {
    if (this.changePasswordForm.valid) {
      const { newPassword, confirmPassword } = this.changePasswordForm.value;

      if (newPassword !== confirmPassword) {
        alert('Passwords do not match');
        return;
      }

      this.userService.changePassword(newPassword).subscribe({
        next: () => {
          alert('Password changed successfully');
          this.dialogRef.close(true);  // Close dialog and return success
        },
        error: () => alert('Failed to change password')
      });
    }
  }

  cancel() {
    this.dialogRef.close(false);  // Close dialog without success
  }
}

