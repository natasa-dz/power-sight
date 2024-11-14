import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {NavbarAdminComponent} from "./navbar-admin/navbar-admin.component";
import {NavbarSuperadminComponent} from "./navbar-superadmin/navbar-superadmin.component";
import {RouterLink, RouterLinkActive, RouterModule} from "@angular/router";
import {MatButtonModule} from "@angular/material/button";
import {MatMenuModule} from "@angular/material/menu";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatIconModule} from "@angular/material/icon";
import {NavbarCitizenComponent} from "./navbar-citizen/navbar-citizen.component";
@NgModule({
  declarations: [
    NavbarSuperadminComponent,
    NavbarAdminComponent,
    NavbarCitizenComponent
  ],
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule,
    RouterLink,
    RouterLinkActive,
    RouterModule,
  ], exports: [
    NavbarSuperadminComponent,
    NavbarAdminComponent,
    NavbarCitizenComponent
  ]
})
export class BaseModule { }
