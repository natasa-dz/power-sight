import { Component } from '@angular/core';
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {MatToolbar} from "@angular/material/toolbar";
import {Router, RouterLink} from "@angular/router";
import {Role} from "../../model/user.model";
import {AuthService} from "../../access-control-module/auth.service";

@Component({
  selector: 'app-navbar-employee',
  standalone: true,
  imports: [
    MatButton,
    MatIcon,
    MatMenu,
    MatMenuItem,
    MatToolbar,
    RouterLink,
    MatMenuTrigger
  ],
  templateUrl: './navbar-employee.component.html',
  styleUrl: './navbar-employee.component.css'
})
export class NavbarEmployeeComponent {
  constructor(private router: Router, private authService:AuthService) {}
  userRole: string = Role.EMPLOYEE;
  userId: number = 0;

  ngOnInit() {
    this.authService.getRoleObservable().subscribe(role=>{
      this.userRole=role;
      console.log(this.userRole);
    })
    this.userId = Number(localStorage.getItem("userId"));
    console.log(this.userId)
  }

  menuItemClicked(option: string) {
    console.log(`Selected option: ${option}`);

    switch (option) {

      case 'logout':
        this.authService.logout().subscribe(() => {
          localStorage.setItem('user',"");
          this.router.navigate(['/login']);
          console.log("Logged out successfully!");
        });
        break;

      default:
        break;
    }
  }
}
