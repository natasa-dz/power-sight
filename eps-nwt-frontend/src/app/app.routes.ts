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
import {ViewEmployeesComponent} from "./employees/view-employees/view-employees.component";
import {EmployeeProfileComponent} from "./employees/employee-profile/employee-profile.component";
import {RegisterEmployeeComponent} from "./employees/register-employee/register-employee.component";
import {EmployeeCalendarComponent} from "./employees/employee-calendar/employee-calendar.component";
import {
  AdminHouseholdRequestsComponent
} from "./requests-module/admin-household-requests/admin-household-requests.component";
import {
  OwnerHouseholdsListingComponent
} from "./requests-module/no-owner-households-listing/owner-households-listing.component";
import {HouseholdRequestComponent} from "./requests-module/household-request/household-request.component";
import {
  OwnerHouseholdRequestsComponent
} from "./requests-module/owner-household-requests/owner-household-requests.component";
import {CityConsumptionComponent} from "./simulators/city-consumption/city-consumption.component";
import {RequestAppointmentComponent} from "./requests-module/request-appointment/request-appointment.component";
import {HouseholdConsumptionComponent} from "./requests-module/household-consumption/household-consumption.component";

export const routes: Routes = [
  { path: 'activate', component: ActivateComponent },  // Activation route
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'real-estate-registration', component: RealEstateRequestComponent },

  { path: 'real-estate-requests/:ownerId/all', component: OwnerRequestListingComponent },
  { path: 'real-estate-requests/admin/requests', component: AdminRequestListingComponent },
  { path: 'real-estate-requests/admin/:requestId', component: RequestViewAdminComponent },

  { path: 'search-households', component: SearchHouseholdComponent },
  { path: 'household/:id', component: ViewHouseholdComponent },
  { path: 'request-appointment', component: RequestAppointmentComponent },
  { path: 'household-consumption', component: HouseholdConsumptionComponent },

  { path: 'manage-household-requests', component: AdminHouseholdRequestsComponent},
  { path: 'household-no-owner', component: OwnerHouseholdsListingComponent},
  { path: 'household-ownership-request/:householdId', component: HouseholdRequestComponent},
  { path: 'my-household-ownership-requests', component: OwnerHouseholdRequestsComponent},


  { path: 'view-employees', component: ViewEmployeesComponent },
  { path: 'employee/:id', component: EmployeeProfileComponent },
  { path: 'register-employee', component: RegisterEmployeeComponent },
  { path: 'employee-calendar', component: EmployeeCalendarComponent },
  { path: 'main', component: MainComponent },
  { path: 'admin/city-consumption', component: CityConsumptionComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },  // Redirect to login by default
  { path: '**', redirectTo: '/login', pathMatch: 'full' }, // Catch-all for undefined paths


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
