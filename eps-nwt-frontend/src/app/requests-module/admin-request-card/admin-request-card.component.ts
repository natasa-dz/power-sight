import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatCard, MatCardContent} from "@angular/material/card";
import {DatePipe, NgIf} from "@angular/common";
import {AllRealEstateRequestsDto} from "../../model/all-real-estate-requests-dto";
import {Router, RouterLink} from "@angular/router";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {format} from "date-fns";

@Component({
  selector: 'app-admin-request-card',
  standalone: true,
    imports: [
      MatCard,
      MatCardContent,
      RouterLink,
      DatePipe,
      NgIf
    ],
  templateUrl: './admin-request-card.component.html',
  styleUrl: './admin-request-card.component.css'
})
export class AdminRequestCardComponent implements OnInit{
  @Input()
  request: AllRealEstateRequestsDto | undefined;

  status: string = "";
  createdAt: string = "";
  finishedAt: string = "";

  constructor(private router: Router, private service: RealEstateRequestService) {
    this.request = {
      id: undefined,
      owner: 0,
      status: undefined,
      createdAt: null,
      finishedAt: null,
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
      if (this.request.finishedAt !== undefined && this.request.finishedAt !== null) {
        this.finishedAt = format(new Date(this.request.finishedAt), 'dd.MM.yyyy.');
      } else{
        this.finishedAt = 'Unknown';
      }
    }
  }
}
