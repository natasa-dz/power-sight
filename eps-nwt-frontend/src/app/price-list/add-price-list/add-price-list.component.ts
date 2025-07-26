import { Component } from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {MapComponent} from "../../requests-module/map/map.component";
import {NgFor, NgForOf, NgIf} from "@angular/common";
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {RouterLink} from "@angular/router";
import {HouseholdRequestDTO} from "../../model/create-household-request-dto.model";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {HttpClient} from "@angular/common/http";
import {MatSnackBar} from "@angular/material/snack-bar";
import {PriceListService} from "../price-list.service";
import {RealEstateRequestDTO} from "../../model/create-real-estate-request-dto.model";
import {RealEstateRequestStatus} from "../../enum/real-estate-request-status";
import {PriceList} from "../../model/price-list";

@Component({
  selector: 'app-add-price-list',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink,
    NgIf,
    NgFor,
    MapComponent,
    BaseModule
  ],
  templateUrl: './add-price-list.component.html',
  styleUrl: './add-price-list.component.css'
})
export class AddPriceListComponent {
  currentStep = 1;
  zonesForm: FormGroup;
  baseAndPdvForm: FormGroup;

  constructor(private fb: FormBuilder,
              private service: PriceListService,
              private http: HttpClient,
              private snackBar: MatSnackBar) {
    this.zonesForm = this.fb.group({
      green: new FormControl('', [
        Validators.required,
        Validators.min(1)
      ]),
      blue: new FormControl('', [
        Validators.required,
        Validators.min(1)
      ]),
      red: new FormControl('', [
        Validators.required,
        Validators.min(1)
      ]),
    });

    this.baseAndPdvForm = this.fb.group({
      base: new FormControl('', [
        Validators.required,
        Validators.min(1)
      ]),
      pdv: new FormControl('', [
        Validators.required,
        Validators.min(0.01),
        Validators.max(1),
      ]),
    });

  }

  goToStep(step: number) {
    this.currentStep = step;
  }

  nextStep() {
    if (this.currentStep < 2) {
      this.currentStep++;
    }
  }

  submit() {
    if (this.zonesForm.valid) {
      if (this.baseAndPdvForm.valid) {

          const priceList: PriceList = {
            greenZone: this.zonesForm.get('green')?.value,
            blueZone: this.zonesForm.get('blue')?.value,
            redZone: this.zonesForm.get('red')?.value,
            basePrice: this.baseAndPdvForm.get('base')?.value,
            pdvPercentage: this.baseAndPdvForm.get('pdv')?.value,
          };
          this.service.addPriceList(priceList).subscribe({
            next:(message: any)=>{
              this.showSnackbar("Success!")
            },
            error: (mess:any) => {
              if(mess.status === 200){
                this.showSnackbar("Success!");
                location.reload();
              } else{
                console.log("Error with creating price list");
              }
            }
          });
      } else {
        this.showSnackbar("Base and pdv form is not valid");
      }
    } else {
      this.showSnackbar("Zone form is not valid");
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
