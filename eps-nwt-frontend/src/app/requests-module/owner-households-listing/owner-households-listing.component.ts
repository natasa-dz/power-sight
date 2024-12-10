import {Component, OnInit} from '@angular/core';
import {HouseholdService} from "../../simulators/household.service";
import {Page} from "../../model/page.model";
import {Household} from "../../model/household.model";
import {BaseModule} from "../../base/base.module";
import {HouseholdDto} from "../../model/householdDTO";
import { ChangeDetectorRef } from '@angular/core';
import {CommonModule, JsonPipe} from "@angular/common";
import {ReactiveFormsModule} from "@angular/forms";
import {Route, Router} from "@angular/router";

@Component({
  selector: 'app-owner-households-listing',
  standalone: true,
  imports: [
    BaseModule,
    JsonPipe,
    CommonModule,
    ReactiveFormsModule,
    // Add this to fix ngIf and ngFor issues

  ],
  templateUrl: './owner-households-listing.component.html',
  styleUrls: ['./owner-households-listing.component.css']
})

export class OwnerHouseholdsListingComponent implements OnInit {

  page: Page<HouseholdDto> = {
    content: [],
    totalPages: 0,
    totalElements: 0,
    size: 5,  // Default page size
    number: 0
  };

  currentPage: number = 1;  // 1-based indexing for display
  pageSize: number = 10;

  constructor(private householdService: HouseholdService, private cdr: ChangeDetectorRef, private router:Router) {}

  ngOnInit(): void {
    this.loadHouseholds();
  }

  loadHouseholds(): void {
    this.householdService
      .getHouseholdsWithoutOwner(this.currentPage - 1, this.pageSize)  // Adjusting for 0-based index
      .subscribe({
        next: (response) => {
          this.page = response;
          console.log("This page content: ", this.page.content)
          this.cdr.detectChanges();  // Ensure update if Angular skips it
        },
        error: (err) => {
          console.error('Failed to fetch households:', err);
        }
      });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadHouseholds();
  }

  onHouseholdClick(household: HouseholdDto): void {
    localStorage.setItem('selectedHousehold', JSON.stringify(household));
    this.router.navigate(['/household-ownership-request', household.id]);
  }
}
