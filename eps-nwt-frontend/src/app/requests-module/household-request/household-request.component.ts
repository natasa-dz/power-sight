import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {BaseModule} from "../../base/base.module";
import {HouseholdService} from "../../simulators/household.service";
import {HttpClient} from "@angular/common/http";
import {formatDate} from "date-fns";
import {HouseholdDto} from "../../model/householdDTO";

@Component({
  selector: 'app-household-request',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    BaseModule,
    FormsModule
  ],
  templateUrl: './household-request.component.html',
  styleUrl: './household-request.component.css'
})

export class HouseholdRequestComponent implements OnInit {

  household!: HouseholdDto;
  uploadedFiles: File[] = [];
  requestNote: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const storedHousehold = localStorage.getItem('selectedHousehold');

    if (storedHousehold) {
      this.household = JSON.parse(storedHousehold);
      console.log('Retrieved household from local storage:', this.household);

    } else {
      // Handle missing household case
      alert('No household data found. Returning to the dashboard.');
      this.router.navigate(['/household-no-owner']);
    }
  }

  onFileSelected(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target?.files) {
      const newFiles = Array.from(target.files);
      this.uploadedFiles = [...this.uploadedFiles, ...newFiles];
    }
  }

  submitRequest(): void {
    if (!this.requestNote.trim() || this.uploadedFiles.length === 0) {
      alert('Please provide a note and attach at least one file.');
      return;
    }

    const formData = new FormData();
    formData.append('householdId', this.household.id.toString());
    formData.append('floor', this.household.floor.toString());
    formData.append('apartmentNumber', this.household.apartmentNumber.toString());
    formData.append('squareFootage', this.household.squareFootage.toString());
    formData.append('requestNote', this.requestNote);
    formData.append('createdAt', formatDate(new Date(), 'yyyy-MM-dd HH:mm:ss'));

    this.uploadedFiles.forEach((file, index) => {
      formData.append(`file${index}`, file, file.name);
    });

    this.http.post('/api/household-requests', formData).subscribe(
      () => {
        alert('Request submitted successfully!');
        this.router.navigate(['/admin-dashboard']);
      },
      (error) => {
        console.error(error);
        alert('Error submitting request. Please try again.');
      }
    );
  }
}

