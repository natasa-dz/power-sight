import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {BaseChartDirective} from "ng2-charts";
import {BaseModule} from "../../base/base.module";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {EmployeeViewDto} from "../../model/view-employee-dto.model";
import {ActivatedRoute} from "@angular/router";
import {EmployeeService} from "../employee.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatProgressSpinner} from "@angular/material/progress-spinner";

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [
    BaseChartDirective,
    BaseModule,
    DecimalPipe,
    FormsModule,
    NgIf,
    NgForOf,
    MatProgressSpinner
  ],
  templateUrl: './employee-profile.component.html',
  styleUrl: './employee-profile.component.css'
})
export class EmployeeProfileComponent implements OnInit {
  employee?: EmployeeViewDto;
  image: string = "";
  loading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.employeeService.findById(+id).subscribe(
        (employee) => {
          this.employee = employee;
          this.employeeService.getProfileImage(employee.userId).subscribe({
            next: (base64Image: string) => {
              this.image = base64Image;
              this.cdr.detectChanges();
            },
            error: (err: any) => {
              console.error('Error loading images', err);
            }
          });
        },
        (error) => {
          console.error("Error fetching household details", error);
        }
      );
    }
  }


  suspend() {
    if(this.employee != undefined && this.employee.id) {
      this.loading = true;
      this.employeeService.suspend(this.employee.id)
        .subscribe(
          (result: Boolean) => {
            if (result) {
              this.showSnackbar("Employee suspended successfuly!")
              location.reload();
              this.loading = false;
            }
          },
          (error: any) => {
            this.showSnackbar("Error suspending employee.");
            console.error(error);
            this.loading = false;
          }
        );
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
