import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {BaseChartDirective} from "ng2-charts";
import {BaseModule} from "../../base/base.module";
import {DatePipe, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {EmployeeViewDto} from "../../model/view-employee-dto.model";
import {ActivatedRoute} from "@angular/router";
import {HouseholdService} from "../../simulators/household.service";
import {WebSocketService} from "../../service/websocket.service";
import {EmployeeService} from "../employee.service";

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [
    BaseChartDirective,
    BaseModule,
    DecimalPipe,
    FormsModule,
    NgIf,
    NgForOf
  ],
  templateUrl: './employee-profile.component.html',
  styleUrl: './employee-profile.component.css'
})
export class EmployeeProfileComponent implements OnInit {
  employee?: EmployeeViewDto;
  image: string = "";

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (typeof id === "string") {
      localStorage.setItem("simulator-id", id);
    }
    if (id) {
      this.employeeService.findById(+id).subscribe(
        (employee) => {
          this.employee = employee;
          this.employeeService.getProfileImage(employee.userPhoto).subscribe({
            next: (base64Image: string) => {
              this.image = base64Image;
              this.cdr.markForCheck();
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
}
