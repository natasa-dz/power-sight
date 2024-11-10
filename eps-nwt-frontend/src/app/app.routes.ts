import {RouterModule, Routes} from '@angular/router';
import {NgModule} from "@angular/core";
import {RegisterComponent} from "./access-control-module/register/register.component";
import {LoginComponent} from "./access-control-module/login/login.component";
import {RealEstateRequestComponent} from "./requests-module/real-estate-request/real-estate-request.component";

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'real-estate-registration', component: RealEstateRequestComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },  // Redirect to login by default
  { path: '**', redirectTo: '/login', pathMatch: 'full' }, // Catch-all for undefined paths


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
