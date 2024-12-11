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
  currentMonthIndex: number = 0;
  currentYear: number = 0;
  days: string[] = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  months: string[] = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  calendarDates: { day: number }[] = [];
  selectedDate: { day: number; month: number; year: number } | null = null;

  constructor(private fb: FormBuilder,
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
          this.initializeCalendar();
        },
        (error) => {
          console.error("Error fetching employee details", error);
        }
      );
    }
  }

  initializeCalendar(): void {
    const today = new Date();
    this.currentMonthIndex = today.getMonth();
    this.currentYear = today.getFullYear();
    this.selectedDate = {
      day: today.getDate(),
      month: today.getMonth(),
      year: today.getFullYear()
    };
    this.generateCalendar(this.currentMonthIndex, this.currentYear);
  }

  generateCalendar(month: number, year: number): void {
    const firstDay = new Date(year, month, 1).getDay();
    const adjustedFirstDay = (firstDay === 0 ? 6 : firstDay - 1);
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    this.calendarDates = [];

    for (let i = 0; i < adjustedFirstDay; i++) {
      this.calendarDates.push({ day: 0 }); // Empty slots for previous month
    }

    for (let day = 1; day <= daysInMonth; day++) {
      this.calendarDates.push({ day });
    }
  }

  prevMonth(): void {
    if (this.currentMonthIndex === 0) {
      this.currentMonthIndex = 11;
      this.currentYear -= 1;
    } else {
      this.currentMonthIndex -= 1;
    }
    this.generateCalendar(this.currentMonthIndex, this.currentYear);
  }

  nextMonth(): void {
    if (this.currentMonthIndex === 11) {
      this.currentMonthIndex = 0;
      this.currentYear += 1;
    } else {
      this.currentMonthIndex += 1;
    }
    this.generateCalendar(this.currentMonthIndex, this.currentYear);
  }

  onDateClick(day: number): void {
    if (day > 0) {
      this.selectedDate = {
        day,
        month: this.currentMonthIndex,
        year: this.currentYear,
      };
      console.log("selected " + this.selectedDate.day + this.selectedDate.month + this.selectedDate.year);
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
