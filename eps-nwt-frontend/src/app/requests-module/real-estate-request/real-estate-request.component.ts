import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterLink} from "@angular/router";
import {CommonModule, NgFor, NgIf} from "@angular/common";

@Component({
  selector: 'app-real-estate-request',
  standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        RouterLink,
        NgIf,
        NgFor
    ],
  templateUrl: './real-estate-request.component.html',
  styleUrl: './real-estate-request.component.css'
})
export class RealEstateRequestComponent {
  currentStep = 1;
  realEstateForm: FormGroup;
  householdForm: FormGroup;
  households: any[] = [];
  documentationFiles: File[] = [];

  constructor(private fb: FormBuilder) {
    this.realEstateForm = this.fb.group({
      address: [''],
      municipality: [''],
      town: [''],
      floors: [''],
      images: [[]],
    });

    this.householdForm = this.fb.group({
      floor: [''],
      squareFootage: [''],
      apartmentNumber: [''],
    });
  }

  goToStep(step: number) {
    this.currentStep = step;
  }

  nextStep() {
    if (this.currentStep < 3) {
      this.currentStep++;
    }
  }

  addHousehold() {
    const household = this.householdForm.value;
    this.households.push(household);
    this.householdForm.reset();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.documentationFiles = Array.from(input.files);
    }
  }

  submit() {

  }
}
