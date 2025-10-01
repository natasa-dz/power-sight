import {Component, OnInit} from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf, NgFor} from "@angular/common";
import {Page} from "../../model/page.model";
import {RouterLink} from "@angular/router";
import {EmployeeSearchDto} from "../../model/employee-search-dto.model";
import {EmployeeService} from "../employee.service";
//import * as console from "node:console";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-view-employees',
  standalone: true,
  imports: [
    BaseModule,
    FormsModule,
    NgForOf,
    NgIf,
    NgFor,
    RouterLink
  ],
  templateUrl: './view-employees.component.html',
  styleUrl: './view-employees.component.css'
})
export class ViewEmployeesComponent implements OnInit {
  username: string = '';
  page: Page<EmployeeSearchDto> = { content: [], totalPages: 0, totalElements: 0, size: 0, number: 0 };
  currentPage: number = 0;
  private debounceTimer: any;
  profilePhotos: { [id: number]: string } = {};

  constructor(private employeeService: EmployeeService,
              private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.loadPage();
  }

  search(): void {
    this.currentPage = 0;
    this.loadPage();
  }

  loadPage(): void {
    this.employeeService.search(this.username, this.currentPage)
      .subscribe(
        (result: Page<EmployeeSearchDto>) => {
          this.page = result;
          this.profilePhotos = {};
          this.page.content.forEach(employee => {
            this.getProfilePhoto(employee.userId);
          });
        },
        (error: any) => {
          this.showSnackbar("Error fetching employees.");
          console.log(error(error))
          //console.error(error);
        }
      );
  }

  getProfilePhoto(id: number){
    this.employeeService.getProfileImage(id).subscribe({
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

  showSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }
}
