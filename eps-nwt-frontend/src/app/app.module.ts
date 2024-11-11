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

import {ChangePasswordComponent} from "./change-password/change-password.component";
import {ActivateComponent} from "./activate/activate.component";
import {MainComponent} from "./main/main.component";
import {BaseModule} from "./base/base.module";  // Import routes from app.routes.ts

@NgModule({
  declarations: [MainComponent,ActivateComponent,AppComponent, ChangePasswordComponent], // Add all components here
  imports: [BaseModule,HttpClientModule,ReactiveFormsModule,RouterModule.forRoot(routes), BrowserModule, AccessControlModule], // Add other necessary Angular modules like FormsModule, etc.
  bootstrap: [AppComponent],
  providers: [provideAnimationsAsync()] // Bootstraps the application with AppComponent
})
export class AppModule {}
