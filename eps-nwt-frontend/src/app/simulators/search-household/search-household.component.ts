import { Component } from '@angular/core';
import {HouseholdSearchDTO} from "../../model/household-search-dto.model";
import {Page} from "../../model/page.model";
import {HouseholdService} from "../household.service";
import {FormsModule} from "@angular/forms";
import {NgFor, NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";
import {BaseModule} from "../../base/base.module";

@Component({
  selector: 'app-search-household',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgFor,
    RouterLink,
    BaseModule
  ],
  templateUrl: './search-household.component.html',
  styleUrl: './search-household.component.css'
})
export class SearchHouseholdComponent {
  municipality: string = '';
  address: string = '';
  apartmentNumber?: number;

  page: Page<HouseholdSearchDTO> = { content: [], totalPages: 0, totalElements: 0, size: 0, number: 0 };
  currentPage: number = 0;

  constructor(private householdService: HouseholdService) {}

  search(): void {
    if (!this.municipality || !this.address) {
      alert("Please enter both municipality and address.");
      return;
    }
    this.householdService.search(this.municipality, this.address, this.apartmentNumber, this.currentPage)
      .subscribe(
        (result: Page<HouseholdSearchDTO>) => {
          this.page = result;
          console.log(result)
        },
        error => {
          alert("Error fetching households.");
          console.error(error);
        }
      );
  }

  goToPage(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.search();
  }
}
