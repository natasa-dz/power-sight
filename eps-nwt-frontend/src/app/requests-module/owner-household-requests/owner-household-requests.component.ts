import {Component, OnInit} from '@angular/core';
import {HouseholdService} from "../../simulators/household.service";
import {CommonModule, DatePipe} from "@angular/common";
import {BaseModule} from "../../base/base.module";

@Component({
  selector: 'app-owner-household-requests',
  standalone: true,
  imports: [
    DatePipe,
    CommonModule,
    BaseModule
  ],
  templateUrl: './owner-household-requests.component.html',
  styleUrl: './owner-household-requests.component.css'
})
export class OwnerHouseholdRequestsComponent implements OnInit {

  username = localStorage.getItem('username');
  householdService;
  ownershipRequests: any[] = [];

  constructor(householdService: HouseholdService) {
    this.householdService = householdService;
  }

  loadUserRequests(): void {
    if (this.username) {
      this.householdService.getUserOwnershipRequests(this.username).subscribe(
        (requests) => {
          this.ownershipRequests = requests;
        },
        (error) => {
          console.error('Error fetching user requests:', error);
          alert('Failed to load your ownership requests.');
        }
      );
    }
  }



  ngOnInit(): void {
    this.loadUserRequests();
  }

  // Method to get the appropriate class for the status
  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'approved';
      case 'PENDING':
        return 'pending';
      case 'REJECTED':
        return 'rejected';
      default:
        return '';
    }
  }
}

