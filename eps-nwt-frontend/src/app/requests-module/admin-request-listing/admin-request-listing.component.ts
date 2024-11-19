import {Component, OnInit} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {AdminRequestCardComponent} from "../admin-request-card/admin-request-card.component";
import {AllRealEstateRequestsDto} from "../../model/all-real-estate-requests-dto";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {BaseModule} from "../../base/base.module";

@Component({
  selector: 'app-admin-request-listing',
  standalone: true,
    imports: [
        FormsModule,
        NgForOf,
        NgIf,
        AdminRequestCardComponent,
        BaseModule
    ],
  templateUrl: './admin-request-listing.component.html',
  styleUrl: './admin-request-listing.component.css'
})
export class AdminRequestListingComponent implements OnInit{
  requests: AllRealEstateRequestsDto[] = [];
  sortType: string = "createdAtAsc";
  filterParams = {
    status: null,
    address: '',
    municipality: '',
    town: '',
    createdFromDate: null,
    createdToDate: null,
    finishedFromDate: null,
    finishedToDate: null
  };
  filteredRequests: AllRealEstateRequestsDto[] = [];
  showFilters = false;

  constructor(private service: RealEstateRequestService) {}

  ngOnInit(): void {
    this.service.getAllRequestsForAdmin().subscribe({
      next: (data: AllRealEstateRequestsDto[]) => {
        this.requests = data
        this.filteredRequests = [...this.requests];
        this.applyFilters();
      },
      error: (_:any) => {
        console.log("Error with all requests for admin!")
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
        this.filteredRequests = this.requests.sort((a, b) => {
          return this.getTime(a.createdAt) - this.getTime(b.createdAt);
        });
        break;
      case 'createdAtDesc':
        console.log("usao u opadajuce")
        this.filteredRequests = this.requests.sort((a, b) => {
          return this.getTime(b.createdAt) - this.getTime(a.createdAt);
        });
        break;
      case 'finishedAtAsc':
        this.filteredRequests = this.requests.sort((a, b) => {
          return this.getTime(a.finishedAt) - this.getTime(b.finishedAt);
        });
        break;
      case 'finishedAtDesc':
        this.filteredRequests = this.requests.sort((a, b) => {
          return this.getTime(b.finishedAt) - this.getTime(a.finishedAt);
        });
        break;
    }
  }

  applyFilters(): void {
    this.filteredRequests = this.requests.filter((request) => {
      // Status filter
      if (this.filterParams.status && request.status !== this.filterParams.status) {
        console.log(this.filterParams.status);
        return false;
      }

      // Address filter
      if (
        this.filterParams.address &&
        !request.address.toLowerCase().includes(this.filterParams.address.toLowerCase())
      ) {
        return false;
      }

      // Municipality filter
      if (
        this.filterParams.municipality &&
        !request.municipality.toLowerCase().includes(this.filterParams.municipality.toLowerCase())
      ) {
        return false;
      }

      // Town filter
      if (
        this.filterParams.town &&
        !request.town.toLowerCase().includes(this.filterParams.town.toLowerCase())
      ) {
        return false;
      }

      // Date filters
      const createdAtTime = this.getTime(request.createdAt);
      const fromDateTimeCreated = this.filterParams.createdFromDate ? new Date(this.filterParams.createdFromDate).getTime() : null;
      const toDateTimeCreated = this.filterParams.createdToDate ? new Date(this.filterParams.createdToDate).getTime() : null;

      if (fromDateTimeCreated && (!createdAtTime || createdAtTime < fromDateTimeCreated)) {
        return false;
      }

      if (toDateTimeCreated && (!createdAtTime || createdAtTime > toDateTimeCreated)) {
        return false;
      }

      const finishedAtTime = this.getTime(request.finishedAt);
      const fromDateTimeFinished = this.filterParams.finishedFromDate ? new Date(this.filterParams.finishedFromDate).getTime() : null;
      const toDateTimeFinished = this.filterParams.finishedToDate ? new Date(this.filterParams.finishedToDate).getTime() : null;

      if (fromDateTimeFinished && (!finishedAtTime || finishedAtTime < fromDateTimeFinished)) {
        return false;
      }

      if (toDateTimeFinished && (!finishedAtTime || finishedAtTime > toDateTimeFinished)) {
        return false;
      }

      return true;
    });
  }

  resetFilters(): void {
    this.filterParams = {
      status: null,
      address: '',
      municipality: '',
      town: '',
      createdFromDate: null,
      createdToDate: null,
      finishedFromDate: null,
      finishedToDate: null
    };
    this.applyFilters();
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }
}
