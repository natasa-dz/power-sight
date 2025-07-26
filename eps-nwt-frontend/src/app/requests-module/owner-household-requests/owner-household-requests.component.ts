import {Component, OnInit} from '@angular/core';
import {HouseholdService} from "../../simulators/household.service";
import {CommonModule, DatePipe} from "@angular/common";
import {BaseModule} from "../../base/base.module";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-owner-household-requests',
  standalone: true,
  imports: [
    DatePipe,
    CommonModule,
    BaseModule,
    FormsModule
  ],
  templateUrl: './owner-household-requests.component.html',
  styleUrl: './owner-household-requests.component.css'
})
export class OwnerHouseholdRequestsComponent implements OnInit {

  username = localStorage.getItem('username');
  householdService;
  ownershipRequests: any[] = [];
  filteredRequests: any[] = [];

  statusFilter: string = '';
  sortCriteria: string = 'submittedAtAsc'; // Default sort


  constructor(householdService: HouseholdService) {
    this.householdService = householdService;
  }

  loadUserRequests(): void {
    if (this.username) {
      this.householdService.getUserOwnershipRequests(this.username).subscribe(
        (requests) => {
          this.ownershipRequests = requests;
          this.filterAndSortRequests(); // Apply initial filter and sort
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

  // Filter and sort the requests based on user input
  filterAndSortRequests(): void {
    let requests = [...this.ownershipRequests];

    // Filter by status
    if (this.statusFilter) {
      requests = requests.filter((request) => request.status === this.statusFilter);
    }

    // Sort by selected criteria
    requests.sort((a, b) => {
      switch (this.sortCriteria) {
        case 'submittedAtAsc':
          return new Date(a.submittedAt).getTime() - new Date(b.submittedAt).getTime();
        case 'submittedAtDesc':
          return new Date(b.submittedAt).getTime() - new Date(a.submittedAt).getTime();
        case 'updatedAtAsc':
          return new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime();
        case 'updatedAtDesc':
          return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
        default:
          return 0;
      }
    });

    this.filteredRequests = requests;
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

