import {Component, OnInit} from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {NgFor, NgForOf, NgIf} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterLink} from "@angular/router";
import {Page} from "../../model/page.model";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CitizenSearchDto} from "../../model/citizen-search-dto";
import {CitizenService} from "../citizen.service";
import {HouseholdAccessDto} from "../../model/household-access-dto.model";
import {Household} from "../../model/household.model";


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
  filteredContent: CitizenSearchDto[] = [];
  currentPage: number = 0;
  private debounceTimer: any;
  loggedIn = "";
  selected: boolean = false;
  selectedHouseholdId : number = 0;
  selectedIds : number[] = [];
  households : HouseholdAccessDto[] = [];
  profilePhotos: { [id: number]: string } = {};

  constructor(private citizenService: CitizenService,
              private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    let username = localStorage.getItem('username');
    if (username != undefined){
      this.loggedIn = username;
    }
    this.loadPage();
    let ownerId = Number(localStorage.getItem('userId'));
    if (ownerId != undefined){
      this.citizenService.getHouseholdsForOwner(ownerId).subscribe({
        next:(data:HouseholdAccessDto[]) => {
          this.households = data;
        }, error: (e:any) => {
          console.log("Error fetching households for owner: ", e)
          this.households = [];
        }
      })
    }

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
          this.filteredContent = this.page.content.filter(citizen => citizen.username !== this.loggedIn);
          this.profilePhotos = {};
          this.filteredContent.forEach(citizen => {
            this.getProfilePhoto(citizen.userId);
          });
        },
        (error: any) => {
          this.showSnackbar("Error fetching citizens.");
          console.error(error);
        }
      );
  }

  getProfilePhoto(id: number){
    this.citizenService.getProfileImage(id).subscribe({
      next: (base64Image: string) => {
        this.profilePhotos[id] = base64Image;
      },
      error: (err: any) => {
        console.error('Error loading images', err);
      }
    });
  }

  goToPage(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadPage();
  }

  debounceSearch(): void {
    clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => this.search(), 400); // 400ms delay
  }

  submitDetails() {
    if (this.selectedHouseholdId != 0){
      this.citizenService.allowAccess(this.selectedHouseholdId, this.selectedIds).subscribe({
        next:(data:string) => {
          this.showSnackbar(data);
        }, error: (e:any) => {
          if (e.status === 200){
            this.showSnackbar(e.error.text);
          }
          else{
            this.showSnackbar("Error");
            console.error(e);
          }
        }
      })
    }
    else {
      this.showSnackbar("You must select household to allow access");
    }
  }

  addToList(citizenId: number) {
    this.selectedIds.push(citizenId);
  }

  removeFromList(citizenId: number) {
    this.selectedIds = this.selectedIds.filter(id => id !== citizenId);
  }

  onHouseholdChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.selectedHouseholdId = Number(target.value);
      this.selected = true;
      let accessGranted = this.households.find(h => h.id === this.selectedHouseholdId)?.accessGranted
      if (accessGranted != undefined){
        this.selectedIds = accessGranted;
      }
    }
  }

  showSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }

}
