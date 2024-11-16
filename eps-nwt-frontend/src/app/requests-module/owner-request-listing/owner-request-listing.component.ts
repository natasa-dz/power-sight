import {Component, OnInit} from '@angular/core';
import {OwnerRequestCardComponent} from "../owner-request-card/owner-request-card.component";
import {AllRealEstateRequestsDto} from "../../model/all-real-estate-requests-dto";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {NgForOf} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-owner-request-listing',
  standalone: true,
    imports: [
        OwnerRequestCardComponent,
        NgForOf,
        FormsModule
    ],
  templateUrl: './owner-request-listing.component.html',
  styleUrl: './owner-request-listing.component.css'
})
export class OwnerRequestListingComponent implements OnInit{
  requests: AllRealEstateRequestsDto[] = [];
  loggedInId : number = 0;
  sortType: string = "createdAtAsc";

  constructor(private service: RealEstateRequestService) {}

  ngOnInit(): void {
    //this.loggedInId = Number(localStorage.getItem("loggedId"));
    this.loggedInId = 1;
    this.service.getAllRequestsForOwner(this.loggedInId).subscribe({
      next: (data: AllRealEstateRequestsDto[]) => {
        this.requests = data
      },
      error: (_:any) => {
        console.log("Error with all requests for owner " + this.loggedInId + " !")
      }
    });
  }

  getTime(date: Date | null) {
    return date != null ? new Date(date).getTime() : 0;
  }

  sortRequests(): void {
    switch (this.sortType) {
      case 'createdAtAsc':
        console.log("usao u rastuce")
        this.requests = this.requests.sort((a, b) => {
            return this.getTime(a.createdAt) - this.getTime(b.createdAt);
        });
        break;
      case 'createdAtDesc':
          console.log("usao u opadajuce")
          this.requests = this.requests.sort((a, b) => {
              return this.getTime(b.createdAt) - this.getTime(a.createdAt);
          });
        break;
      case 'approvedAtAsc':
          this.requests = this.requests.sort((a, b) => {
              return this.getTime(a.approvedAt) - this.getTime(b.approvedAt);
          });
        break;
      case 'approvedAtDesc':
          this.requests = this.requests.sort((a, b) => {
              return this.getTime(b.approvedAt) - this.getTime(a.approvedAt);
          });
        break;
    }
  }
}
