import {RouterModule, Routes} from '@angular/router';
import {NgModule} from "@angular/core";
import {RegisterComponent} from "./access-control-module/register/register.component";
import {LoginComponent} from "./access-control-module/login/login.component";
import {ActivateComponent} from "./activate/activate.component";
import {MainComponent} from "./main/main.component";

export const routes: Routes = [
  { path: 'activate', component: ActivateComponent },  // Activation route
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'main', component: MainComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },  // Redirect to login by default
  { path: '**', redirectTo: '/login', pathMatch: 'full' }, // Catch-all for undefined paths


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
