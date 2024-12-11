import {Component, OnInit} from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf, NgFor} from "@angular/common";
import {Page} from "../../model/page.model";
import {RouterLink} from "@angular/router";
import {EmployeeSearchDto} from "../../model/employee-search-dto.model";
import {EmployeeService} from "../employee.service";
import * as console from "node:console";

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

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.employeeService.getAll(this.currentPage)
      .subscribe(
        (result: Page<EmployeeSearchDto>) => {
          this.page = result;
          console.log(result)
        },
        (error: any) => {
          alert("Error fetching employes.");
          console.error(error);
        }
      );
  }

  search(): void {
    if (!this.username) {
      this.employeeService.getAll(this.currentPage)
        .subscribe(
          (result: Page<EmployeeSearchDto>) => {
            this.page = result;
            console.log(result)
          },
          (error: any) => {
            alert("Error fetching employes.");
            console.error(error);
          }
        );
      return;
    }
    this.employeeService.search(this.username, this.currentPage)
      .subscribe(
        (result: Page<EmployeeSearchDto>) => {
          this.page = result;
          console.log(result)
        },
        (error: any) => {
          alert("Error fetching employes.");
          console.error(error);
        }
      );
  }

  goToPage(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.search();
  }
}
