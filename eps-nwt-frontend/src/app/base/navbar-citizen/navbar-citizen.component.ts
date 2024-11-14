import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "../../access-control-module/auth.service";
import {Role} from "../../model/user.model";

@Component({
  selector: 'app-navbar-citizen',
  templateUrl: './navbar-citizen.component.html',
  styleUrls: ['./navbar-citizen.component.css']
})
export class NavbarCitizenComponent implements OnInit{

  constructor(private router: Router, private authService:AuthService) {}
  userRole: string = Role.CITIZEN;

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

      default:
        break;
    }
  }

}
