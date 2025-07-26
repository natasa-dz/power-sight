import {Component, Input, OnInit} from '@angular/core';
import {Receipt} from "../../model/receipt.model";
import {MatCard, MatCardContent} from "@angular/material/card";
import {Router, RouterLink} from "@angular/router";
import {DatePipe, NgIf} from "@angular/common";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {format} from "date-fns";

@Component({
  selector: 'app-receipts-card',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    RouterLink,
    DatePipe,
    NgIf
  ],
  templateUrl: './receipts-card.component.html',
  styleUrl: './receipts-card.component.css'
})
export class ReceiptsCardComponent implements OnInit{
  @Input()
  receipt: Receipt | undefined;

  status: string = "";
  consumption: number = 0;
  finishedAt: string = "";


  constructor() {
    this.receipt = {
      id: 0,
      ownerId: 0,
      paid: false,
      month: "null",
      year: 0,
      paymentDate: null,
      householdAddress: '',
      householdApartmentNumber: 0,
      ownerUsername: '',
      price: 0,
      greenZoneConsumption: 0,
      blueZoneConsumption: 0,
      redZoneConsumption: 0,
      priceList: {
        greenZone: 0,
        blueZone: 0,
        redZone: 0,
        basePrice: 0,
        pdvPercentage: 0
      },
      householdId: 0,
      path: ''
    }
  }

  ngOnInit(): void {
    if (this.receipt !== undefined){
      this.status = this.receipt.paid ? 'Paid' : 'Not paid';
      this.consumption = this.receipt.blueZoneConsumption + this.receipt.greenZoneConsumption + this.receipt.redZoneConsumption;
      if (this.receipt.paymentDate !== undefined && this.receipt.paymentDate !== null) {
        this.finishedAt = format(new Date(this.receipt.paymentDate), 'dd.MM.yyyy.');
      } else{
        this.finishedAt = 'Unknown';
      }
    }
  }
}
