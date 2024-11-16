import {Component, Input, OnInit} from '@angular/core';
import {MatCard, MatCardContent} from "@angular/material/card";
import {AllRealEstateRequestsDto} from "../../model/all-real-estate-requests-dto";
import {Router, RouterLink} from "@angular/router";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {DatePipe, NgIf} from "@angular/common";
import {format} from "date-fns";

@Component({
  selector: 'app-owner-request-card',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    RouterLink,
    DatePipe,
    NgIf
  ],
  templateUrl: './owner-request-card.component.html',
  styleUrl: './owner-request-card.component.css'
})
export class OwnerRequestCardComponent implements OnInit{
  @Input()
  request: AllRealEstateRequestsDto | undefined;

  //@Output()
  //clicked: EventEmitter<AccommodationListingDto> = new EventEmitter<AccommodationListingDto>();

  status: string = "";
  createdAt: string = "";
  approvedAt: string = "";

  constructor(private router: Router, private service: RealEstateRequestService) {
    this.request = {
      id: undefined,
      owner: 0,
      status: undefined,
      createdAt: null,
      approvedAt: null,
      address: '',
      municipality: '',
      town: '',
    }
  }

  ngOnInit(): void {
    if (this.request !== undefined){
      if (this.request.status !== undefined) {
        this.status = this.request.status.toString();
      }
      if (this.request.createdAt !== null) {
        this.createdAt = format(new Date(this.request.createdAt), 'dd.MM.yyyy.');
      } else{
        this.createdAt = 'Unknown';
      }
      if (this.request.approvedAt !== null) {
        this.approvedAt = format(new Date(this.request.approvedAt), 'dd.MM.yyyy.');
      } else{
        this.approvedAt = 'Unknown';
      }
    }
  }
}
