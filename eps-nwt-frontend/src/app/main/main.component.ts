import {Component, OnInit} from '@angular/core';
import {Role} from "../model/user.model";
import {NavbarCitizenComponent} from "../base/navbar-citizen/navbar-citizen.component";
import {BaseModule} from "../base/base.module";
import {AuthService} from "../access-control-module/auth.service";
import {Router} from "@angular/router";
import {UserService} from "../service/user.service";

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrl: './main.component.css'
})
export class MainComponent implements OnInit{

  protected readonly Role = Role;
  userRole!: Role;
  constructor(private router: Router, private userService: UserService,
              private authService:AuthService) {}

  async ngOnInit(): Promise<void> {
    this.userRole =this.authService.getRole();
  }
}
