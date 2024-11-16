import {Component, OnInit} from '@angular/core';
import {OwnerRequestCardComponent} from "../owner-request-card/owner-request-card.component";
import {AllRealEstateRequestsDto} from "../../model/all-real-estate-requests-dto";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-owner-request-listing',
  standalone: true,
  imports: [
    OwnerRequestCardComponent,
    NgForOf
  ],
  templateUrl: './owner-request-listing.component.html',
  styleUrl: './owner-request-listing.component.css'
})
export class OwnerRequestListingComponent implements OnInit{
  requests: AllRealEstateRequestsDto[] = [];
  loggedInId : number = 0;

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
}
