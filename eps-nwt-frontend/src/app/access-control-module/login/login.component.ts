import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {UserService} from "../../service/user.service";
import {AuthService} from "../auth.service";
import {MatDialog} from "@angular/material/dialog";
import {ChangePasswordComponent} from "../../change-password/change-password.component";
import {Role} from "../../model/user.model";
import {JwtHelperService} from "@auth0/angular-jwt";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']  // Make sure it's styleUrls (plural)
})

export class LoginComponent implements OnInit {
  @Input() title: string = "Login to Electro Power";
  @Input() primaryBtnText: string = "Sign In";
  @Input() secondaryBtnText: string = "Sign Up";
  @Input() disablePrimaryBtn: boolean = true;
  @Output() onSubmit = new EventEmitter();
  @Output("navigate") onNavigate = new EventEmitter();

  loginForm!: FormGroup;
  currentUser: any;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    public dialog: MatDialog  // Inject MatDialog service
  ) {}
  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  login() {

    if (this.loginForm.valid) {
      const loginData = {
        email: this.loginForm.value.email,
        password: this.loginForm.value.password
      };

      this.authService.login(loginData).subscribe({
        next: (response) => {
          if (!response) {
            alert("Account not verified yet!");
          } else {

            localStorage.setItem('user', response.accessToken);
            this.userService.setUserDetails();

            const helper = new JwtHelperService();
            const decoded = helper.decodeToken(response.accessToken);
            this.userService.getCurrentUser().subscribe((user: any) => {
              this.currentUser = user;
              console.log(user);
              console.log("IsActive: ", user.active)
             if (user.role === Role.SUPERADMIN && !user.passwordChanged) {
               alert('Please change your default password.');
               this.openChangePasswordDialog();
              }
              else if (!user.active) {
                alert('Account not activated. Please check your email.');
                this.authService.logout();
              }
              else {
                if(user.role === Role.ADMIN){
                  this.router.navigate(['real-estate-requests/admin/requests']);
                } else{
                  this.router.navigate(['main']);
                }
              }
            });
          }
        },
        error: (err) => {
          if (err.status === 403) {
            alert('Your account is suspended!');
          } else {
            console.log("Greska je: ",err);
            alert('Bad credentials or account not verified yet');
          }
        }
      });
    } else {
      alert('Please provide valid credentials');
    }
  }

  openChangePasswordDialog() {
    const dialogRef = this.dialog.open(ChangePasswordComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        alert('Password changed successfully. You can now proceed.');
        this.router.navigate(['main']);  // Redirect to main page after successful password change
      } else {
        alert('Password change is required to continue.');
        this.authService.logout();
      }
    });
  }


  navigate(){
    this.onNavigate.emit();
  }
}
