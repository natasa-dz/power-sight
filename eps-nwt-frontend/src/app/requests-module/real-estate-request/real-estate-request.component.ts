import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {RouterLink} from "@angular/router";
import {CommonModule, NgFor, NgIf} from "@angular/common";
import {HouseholdRequestDTO} from "../../model/create-household-request-dto.model";
import {RealEstateRequestDTO} from "../../model/create-real-estate-request-dto.model";
import {RealEstateRequestStatus} from "../../enum/real-estate-request-status";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {HttpClient} from "@angular/common/http";
import {MapComponent} from "../map/map.component";

@Component({
  selector: 'app-real-estate-request',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    NgIf,
    NgFor,
    MapComponent
  ],
  templateUrl: './real-estate-request.component.html',
  styleUrl: './real-estate-request.component.css'
})
export class RealEstateRequestComponent implements OnInit{
  currentStep = 1;
  realEstateForm: FormGroup;
  householdForm: FormGroup;
  households: HouseholdRequestDTO[] = [];
  documentationFiles: File[] = [];
  citiesAndMunicipalities: any = {};
  cities: string[] = [];
  selectedCity: string = '';
  selectedMunicipality: string = '';
  loggedInId : number = 0;

  constructor(private fb: FormBuilder,
              private service: RealEstateRequestService,
              private http: HttpClient) {
    this.realEstateForm = this.fb.group({
      address: new FormControl('', [Validators.required]),
      municipality: new FormControl('', [Validators.required]),
      city: new FormControl('', [Validators.required]),
      floors: new FormControl('', [Validators.required,
        Validators.min(0)]),
      images: new FormControl([], [this.minArrayLength(1)]),
    });

    this.householdForm = this.fb.group({
      floor: new FormControl('', [Validators.required,
        Validators.min(0)]),
      squareFootage: new FormControl('', [Validators.required,
        Validators.min(10)]),
      apartmentNumber: new FormControl('', [Validators.required,
        Validators.min(1)]),
    });
  }

  ngOnInit() {
    this.service.getCitiesWithMunicipalities().subscribe(data => {
      this.citiesAndMunicipalities = data;
      this.cities = Object.keys(this.citiesAndMunicipalities);
    });
    this.loggedInId = Number(localStorage.getItem("userId"));
  }

  onCityChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.selectedCity = target.value;
      this.selectedMunicipality = '';
    }
  }

  onAddressChange(newAddress: string): void {
    this.realEstateForm.get('address')?.setValue(newAddress);
  }


  minArrayLength(min: number): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (Array.isArray(value) && value.length >= min) {
        return null; // validno
      }
      return { minArrayLength: { requiredLength: min, actualLength: value.length } };
    };
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
    if (this.householdForm.valid){
      const household : HouseholdRequestDTO = {
        floor: this.householdForm.get('floor')?.value,
        squareFootage: this.householdForm.get('squareFootage')?.value,
        apartmentNumber: this.householdForm.get('apartmentNumber')?.value
      }
      this.households.push(household);
      this.householdForm.reset();
    } else {
      alert("Bad input")
    }

  }

  onFileSelected1(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const filesArray = Array.from(input.files);
      this.realEstateForm.get('images')?.setValue(filesArray);
    }
  }

  onFileSelected2(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.documentationFiles = Array.from(input.files);
    }
  }

  submit() {
    if (this.realEstateForm.valid) {
      if (this.households.length > 0) {
        if (this.documentationFiles.length >= 1) {

          const realEstateRequest: RealEstateRequestDTO = {
            owner: this.loggedInId,
            address: this.realEstateForm.get('address')?.value,
            municipality: this.realEstateForm.get('municipality')?.value,
            town: this.realEstateForm.get('city')?.value,
            floors: this.realEstateForm.get('floors')?.value,
            images: null,
            documentation: null,
            status: RealEstateRequestStatus.WAITING,
            householdRequests: this.households,
            createdAt: new Date(),
            finishedAt: null,
            adminNote: ''
          };
          this.service.createRequest(realEstateRequest, this.realEstateForm.get('images')?.value, this.documentationFiles).subscribe({
            next:(message:string)=>{
              console.log(message);
            },
            error: (mess:any) => {
              if(mess.status === 200){
                alert(mess.error.text);
                location.reload();
              } else{
                console.log("Error with creating real estate request");
              }
            }
          });


        } else {
          alert("Nije u redu dokumentacija");
        }
      } else {
        alert("Nije u redu forma za household");
      }
    } else {
      alert("Nije u redu forma za nekretninu");
    }
  }
}
