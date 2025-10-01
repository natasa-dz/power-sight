// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {AppComponent} from "./app.component";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {AccessControlModule} from "./access-control-module/access-control.module";
import {RouterModule} from "@angular/router";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async'; // Adjust the path to your component
import { routes } from './app.routes';
import {RealEstateRequestComponent} from "./requests-module/real-estate-request/real-estate-request.component";
import {RequestsModuleModule} from "./requests-module/requests-module.module";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";  // Import routes from app.routes.ts
import { ReactiveFormsModule } from '@angular/forms';
import {ChangePasswordComponent} from "./change-password/change-password.component";
import {SearchHouseholdComponent} from "./simulators/search-household/search-household.component";  // Import routes from app.routes.ts
import {ActivateComponent} from "./activate/activate.component";
import {MainComponent} from "./main/main.component";
import {NavbarEmployeeComponent} from "./base/navbar-employee/navbar-employee.component";  // Import routes from app.routes.ts
import {BaseModule} from "./base/base.module";  // Import routes from app.routes.ts
import { MatSnackBarModule } from '@angular/material/snack-bar';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {Interceptor} from "./access-control-module/interceptor";
import {SharedModule} from "./shared/shared.module";

@NgModule({
  declarations: [AppComponent,
    ChangePasswordComponent,
    MainComponent,
    ActivateComponent], // Add all components here
  imports: [RouterModule.forRoot(routes),
    BrowserModule,
    AccessControlModule,
    RequestsModuleModule,
    CommonModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routes),
    BrowserModule,
    AccessControlModule,
    BrowserAnimationsModule,
    MatSnackBarModule,
    NavbarEmployeeComponent,
    SearchHouseholdComponent,
    BaseModule,
    SharedModule],
  bootstrap: [AppComponent],
  providers: [provideAnimationsAsync(), {
    provide: HTTP_INTERCEPTORS,
    useClass: Interceptor,
    multi: true
  }]
})
export class AppModule {}
