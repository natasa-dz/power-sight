import { Component } from '@angular/core';
import {UserService} from "../../service/user.service";
import {HouseholdService} from "../../simulators/household.service";
import {FormsModule} from "@angular/forms";
import {CommonModule, NgForOf, NgIf} from "@angular/common";
import {BaseModule} from "../../base/base.module";
import {SharedModule} from "../../shared/shared.module";

@Component({
  selector: 'app-admin-household-requests',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgForOf,
    CommonModule,
    BaseModule,
    SharedModule
  ],
  templateUrl: './admin-household-requests.component.html',
  styleUrl: './admin-household-requests.component.css'
})
export class AdminHouseholdRequestsComponent {

  householdService:HouseholdService;
  pendingRequests: any[] = [];
  rejectionReason: string = '';
  constructor(householdService:HouseholdService){
    this.householdService=householdService;
  }
  ngOnInit(): void {
    this.loadPendingRequests();
  }

  loadPendingRequests(): void {
    this.householdService.getPendingRequests().subscribe(
      (requests) => (this.pendingRequests = requests),
      (error) => console.error('Error loading requests', error)
    );
  }
  submitRequestDecision(requestId: number, approved: boolean, reason: string = ''): void {
    if (!approved && !reason.trim()) {
      alert('Please provide a reason for rejection.');
      return;
    }

    this.householdService.processRequest(requestId, approved, reason).subscribe(
      () => {
        alert(`Request ${approved ? 'approved' : 'rejected'} successfully!`);
        this.loadPendingRequests();
      },
      (error) => {
        console.error(error);
        alert('Error processing request. Please try again.');
      }
    );
  }

  loadSubmittedFiles(requestId: number): void {
    this.householdService.getSubmittedFiles(requestId).subscribe({
      next: (files) => {
        const request = this.pendingRequests.find(r => r.id === requestId);
        if (request) {
          request.files = files;
        }
      },
      error: (err) => console.error('Error loading files', err)
    });
  }


}
