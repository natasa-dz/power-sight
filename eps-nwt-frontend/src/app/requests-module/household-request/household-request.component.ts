import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {BaseModule} from "../../base/base.module";
import {HouseholdService} from "../../simulators/household.service";
import {HttpClient} from "@angular/common/http";
import {formatDate} from "date-fns";
import {HouseholdDto} from "../../model/householdDTO";
import {AuthService} from "../../access-control-module/auth.service";
import {UserService} from "../../service/user.service";
import {User} from "../../model/user.model";

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
    private http: HttpClient,
    private userService:UserService,
    private householdService: HouseholdService
  ) {}

  ngOnInit(): void {
    const storedHousehold = localStorage.getItem('selectedHousehold');

    if (storedHousehold) {
      this.household = JSON.parse(storedHousehold);
      console.log('Retrieved household from local storage:', this.household);

    } else {
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


  removeFile(index: number): void {
    this.uploadedFiles.splice(index, 1);
  }

  submitRequest(): void {
    if (!this.requestNote.trim() || this.uploadedFiles.length === 0) {
      alert('Please provide a note and attach at least one file.');
      return;
    }

    const username = localStorage.getItem('username');
    if(username){
      this.householdService.submitOwnershipRequest(username, this.household.id, this.uploadedFiles)
      .subscribe(
        () => {
          alert('Request submitted successfully!');
          this.router.navigate(['/main']);
        },
        (error: any) => {
          console.error(error);
          alert('Error submitting request. Please try again.');
        }
      );
    }
  }
}

