// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {AppComponent} from "./app.component";
import {HttpClientModule} from "@angular/common/http";
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
import {BaseModule} from "./base/base.module";  // Import routes from app.routes.ts

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
    SearchHouseholdComponent,
    BaseModule],
  bootstrap: [AppComponent],
  providers: [provideAnimationsAsync()] // Bootstraps the application with AppComponent
})
export class AppModule {}
