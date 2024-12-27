import { Component } from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-household-consumption',
  standalone: true,
  imports: [
    BaseModule,
    CommonModule,
    FormsModule
  ],
  templateUrl: './household-consumption.component.html',
  styleUrl: './household-consumption.component.css'
})
export class HouseholdConsumptionComponent {

}
