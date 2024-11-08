// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {AppComponent} from "./app.component";
import {HttpClientModule} from "@angular/common/http";
import {AccessControlModule} from "./access-control-module/access-control.module";
import {RouterModule} from "@angular/router";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async'; // Adjust the path to your component
import { routes } from './app.routes';
import { ReactiveFormsModule } from '@angular/forms';

import {ChangePasswordComponent} from "./change-password/change-password.component";  // Import routes from app.routes.ts

@NgModule({
  declarations: [AppComponent, ChangePasswordComponent], // Add all components here
  imports: [HttpClientModule,ReactiveFormsModule,RouterModule.forRoot(routes), BrowserModule, AccessControlModule], // Add other necessary Angular modules like FormsModule, etc.
  bootstrap: [AppComponent],
  providers: [provideAnimationsAsync()] // Bootstraps the application with AppComponent
})
export class AppModule {}
