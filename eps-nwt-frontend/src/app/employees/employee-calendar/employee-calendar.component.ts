import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {EmployeeService} from "../employee.service";
import {BaseModule} from "../../base/base.module";
import {DatePipe, NgFor, NgForOf, NgIf} from "@angular/common";
import {EmployeeViewDto} from "../../model/view-employee-dto.model";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-employee-calendar',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    BaseModule,
    NgIf,
    DatePipe,
    NgFor,
    NgForOf
  ],
  templateUrl: './employee-calendar.component.html',
  styleUrl: './employee-calendar.component.css'
})
export class EmployeeCalendarComponent implements OnInit {
  employee?: EmployeeViewDto;
  private loggedInId: number;
  appointmentForm: FormGroup;
  availableSlots: string[] = [];
  isLoading: boolean = false;
  message: string = '';

  constructor(private route: ActivatedRoute,
              private fb: FormBuilder,
              private employeeService: EmployeeService,
  ) {
    this.appointmentForm = this.fb.group({
      employeeId: [''],
      date: [''],
      startTime: [''],
      timeSlotCount: [1]
    });
    this.loggedInId = Number(localStorage.getItem("userId"));
  }

  ngOnInit(): void {
    this.loggedInId = Number(localStorage.getItem("userId"));
    if (this.loggedInId) {
      this.employeeService.findByUserId(this.loggedInId).subscribe(
        (employee) => {
          this.employee = employee;
        },
        (error) => {
          console.error("Error fetching employee details", error);
        }
      );
    }
  }

  fetchAvailableSlots() {
    const employeeId = this.employee?.id;
    const dateValue  = this.appointmentForm.get('date')?.value;
    const date = new Date(dateValue);
    const formattedDate = date.toISOString().split('T')[0];

    if (employeeId && date) {
      this.isLoading = true;
      this.employeeService.getAvailableSlots(employeeId, formattedDate).subscribe(
        (slots) => {
          this.availableSlots = slots.map(slot => new Date(slot).toTimeString().slice(0, 5));
          this.isLoading = false;
        },
        (error) => {
          console.error('Error fetching slots:', error);
          this.message = 'Failed to fetch available slots. Please try again.';
          this.isLoading = false;
        }
      );
    }
  }

  bookAppointment() {
    if(this.appointmentForm.valid) {
      const formValues = this.appointmentForm.value;
      const dateValue  = this.appointmentForm.get('date')?.value;
      const date = new Date(dateValue);
      const formattedDate = date.toISOString().split('T')[0];
      const startTime = `${formattedDate}T${formValues.startTime}`;
      if(this.employee != undefined) {
        const payload = {
          employeeId: this.employee.id,
          userId: this.loggedInId,
          startTime: startTime,
          timeSlotCount: formValues.timeSlotCount,
        };

        this.employeeService.bookAppointment(payload).subscribe(
          (response) => {
            this.message = response;
            this.availableSlots = [];
            this.appointmentForm.reset();
          },
          (error) => {
            this.appointmentForm.reset();
            console.error('Error booking appointment:', error.error);
            this.message = error.error;
          }
        );
      }
    } else {
      alert("please fill out all fields");
    }
  }

}
