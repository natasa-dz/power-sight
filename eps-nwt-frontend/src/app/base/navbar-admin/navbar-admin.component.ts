import { Component } from '@angular/core';
import {Role} from "../../model/user.model";

@Component({
  selector: 'app-navbar-admin',
  templateUrl: './navbar-admin.component.html',
  styleUrls: ['./navbar-admin.component.css']
})
export class NavbarAdminComponent {
  userRole: string = Role.ADMIN;


}
