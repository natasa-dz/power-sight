import {RouterModule, Routes} from '@angular/router';
import {NgModule} from "@angular/core";
import {RegisterComponent} from "./access-control-module/register/register.component";
import {LoginComponent} from "./access-control-module/login/login.component";
import {RealEstateRequestComponent} from "./requests-module/real-estate-request/real-estate-request.component";
import {SearchHouseholdComponent} from "./simulators/search-household/search-household.component";
import {ViewHouseholdComponent} from "./simulators/view-household/view-household.component";
import {ActivateComponent} from "./activate/activate.component";
import {MainComponent} from "./main/main.component";
import {OwnerRequestListingComponent} from "./requests-module/owner-request-listing/owner-request-listing.component";
import {AdminRequestListingComponent} from "./requests-module/admin-request-listing/admin-request-listing.component";
import {RequestViewAdminComponent} from "./requests-module/request-view-admin/request-view-admin.component";

export const routes: Routes = [
  { path: 'activate', component: ActivateComponent },  // Activation route
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'real-estate-registration', component: RealEstateRequestComponent },
  { path: 'real-estate-requests/:ownerId/all', component: OwnerRequestListingComponent },
  { path: 'real-estate-requests/admin/requests', component: AdminRequestListingComponent },
  { path: 'real-estate-requests/:requestId', component: RequestViewAdminComponent },
  { path: 'search-households', component: SearchHouseholdComponent },
  { path: 'household/:id', component: ViewHouseholdComponent },
  { path: 'main', component: MainComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },  // Redirect to login by default
  { path: '**', redirectTo: '/login', pathMatch: 'full' }, // Catch-all for undefined paths


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
