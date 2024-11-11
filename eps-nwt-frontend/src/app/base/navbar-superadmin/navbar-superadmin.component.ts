import { Component } from '@angular/core';
import {Role} from "../../model/user.model";
import {AuthService} from "../../access-control-module/auth.service";
import {Router, RouterLink} from "@angular/router";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {MatIcon} from "@angular/material/icon";
import {MatToolbar} from "@angular/material/toolbar";
import {MatButton} from "@angular/material/button";

@Component({
  selector: 'app-navbar-superadmin',
  templateUrl: './navbar-superadmin.component.html',
  styleUrl: './navbar-superadmin.component.css'
})
export class NavbarSuperadminComponent {

  userRole: string = Role.SUPERADMIN;

  constructor(private router: Router, private authService:AuthService) {}

  ngOnInit() {
    this.authService.getRoleObservable().subscribe(role=>{
      this.userRole=role;
      console.log(this.userRole);
    })
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
      case 'add-admin':
        this.router.navigate(['/manage']);
        break;
      // case 'reviews/apartments/approval':
      //   this.router.navigate(['reviews/apartments/approval']);
      //   break;
      // case 'reviews/owners/approval':
      //   this.router.navigate(['reviews/owners/approval']);
      //   break;
      // case 'accommodation-approval':
      //   this.router.navigate(['/accommodation-approval'])
      //   break;
      // //TODO:
      // case 'get-all-certificates':
      //   this.router.navigate(['/allCertificates']);
      //   break;
      // case 'generate-certificate':
      //   this.router.navigate(['/generateCertificate']);
      //   break;
      // case 'user-blocking':
      //   this.router.navigate(['/user-blocking'])
      //   break;
      default:
        break;
    }
  }


}
