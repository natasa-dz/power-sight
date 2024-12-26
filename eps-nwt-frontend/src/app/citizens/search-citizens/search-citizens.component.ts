import {Component, OnInit} from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {NgFor, NgForOf, NgIf} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterLink} from "@angular/router";
import {Page} from "../../model/page.model";
import {MatSnackBar} from "@angular/material/snack-bar";
import console from "node:console";
import {CitizenSearchDto} from "../../model/citizen-search-dto";
import {CitizenService} from "../citizen.service";

@Component({
  selector: 'app-search-citizens',
  standalone: true,
  imports: [
    BaseModule,
    FormsModule,
    NgForOf,
    NgIf,
    NgFor,
    RouterLink
  ],
  templateUrl: './search-citizens.component.html',
  styleUrl: './search-citizens.component.css'
})
export class SearchCitizensComponent implements OnInit{
  username: string = '';
  page: Page<CitizenSearchDto> = { content: [], totalPages: 0, totalElements: 0, size: 0, number: 0 };
  currentPage: number = 0;
  private debounceTimer: any;

  constructor(private citizenService: CitizenService,
              private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.loadPage();
  }

  search(): void {
    this.currentPage = 0;
    this.loadPage();
  }

  loadPage(): void {
    this.citizenService.search(this.username, this.currentPage)
      .subscribe(
        (result: Page<CitizenSearchDto>) => {
          this.page = result;
        },
        (error: any) => {
          this.showSnackbar("Error fetching citizens.");
          console.error(error);
        }
      );
  }

  goToPage(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadPage();
  }

  debounceSearch(): void {
    clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => this.search(), 400); // 400ms delay
  }

  showSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }
}
