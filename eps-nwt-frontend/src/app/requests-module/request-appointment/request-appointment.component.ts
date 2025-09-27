import { Component } from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {DatePipe, NgFor, NgForOf, NgIf} from "@angular/common";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {EmployeeService} from "../../employees/employee.service";
import {EmployeeViewDto} from "../../model/view-employee-dto.model";
import {Appointment} from "../../model/appointment.model";
import {AppointmentStatus} from "../../enum/appointment-status";

@Component({
  selector: 'app-request-appointment',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    BaseModule,
    NgIf,
    DatePipe,
    NgFor,
    NgForOf,
    FormsModule
  ],
  templateUrl: './request-appointment.component.html',
  styleUrl: './request-appointment.component.css'
})
export class RequestAppointmentComponent {
  employees: EmployeeViewDto[] = [];
  selectedEmployeeId: number | null = null;
  employee?: EmployeeViewDto;
  protected loggedInId: number;
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
  appointments: Appointment[] = [];

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
        this.loadEmployees();
        this.initializeCalendar();
        this.fetchAppointmentsForDate();
  }

  loadEmployees(): void {
    this.employeeService.getAllEmployeesNoPagination().subscribe({
      next: (data) => this.employees = data,
      error: (err) => console.error('Error loading employees', err)
    });
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
      this.fetchAppointmentsForDate();
    }
  }

  fetchAppointmentsForDate(): void {
    if (this.selectedDate && this.selectedEmployeeId) {
      const dateString = `${this.selectedDate.year}-${String(this.selectedDate.month + 1).padStart(2, '0')}-${String(this.selectedDate.day).padStart(2, '0')}`;
      this.employeeService.getAppointmentsForDate(this.selectedEmployeeId, dateString).subscribe(
        (appointments) => {
          this.appointments = appointments.sort((a, b) => {
            const timeA = a.startTime;
            const timeB = b.startTime;
            if (timeA < timeB) {
              return -1;
            } else if (timeA > timeB) {
              return 1;
            } else {
              return 0;
            }
          });
        },
        (error) => {
          console.error('Error fetching appointments:', error);
          this.appointments = [];
        }
      );
    }
  }

  fetchAvailableSlots() {
    const employeeId = this.appointmentForm.get('employeeId')?.value;
    const dateValue  = this.appointmentForm.get('date')?.value;
    const date = new Date(dateValue);
    const formattedDate = date.toISOString().split('T')[0];

    if (this.selectedEmployeeId && date) {
      this.isLoading = true;
      this.employeeService.getAvailableSlots(this.selectedEmployeeId, formattedDate).subscribe(
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
    if(this.appointmentForm.valid && this.selectedEmployeeId) {
      const formValues = this.appointmentForm.value;
      const dateValue  = this.appointmentForm.get('date')?.value;
      const date = new Date(dateValue);
      const formattedDate = date.toISOString().split('T')[0];
      const startTime = `${formattedDate}T${formValues.startTime}`;
        const payload = {
          employeeId: this.selectedEmployeeId,
          userId: this.loggedInId,
          startTime: startTime,
          timeSlotCount: 1,
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

    } else {
      alert("please fill out all fields");
    }
  }

  getStatusFormatted(status: AppointmentStatus) {
    if(status.valueOf() == AppointmentStatus.CREATED.valueOf()) return "created";
    if(status.valueOf() == AppointmentStatus.CANCELLED.valueOf()) return "cancelled";
    if(status.valueOf() == AppointmentStatus.FINISHED.valueOf()) return "finished";
    return "";
  }
}
