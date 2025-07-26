import {Component, OnInit} from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {Receipt} from "../../model/receipt.model";
import {ActivatedRoute, Router} from "@angular/router";
import {PriceListService} from "../price-list.service";
import {format} from "date-fns";

@Component({
  selector: 'app-receipt-view',
  standalone: true,
  imports: [
    BaseModule,
    FormsModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './receipt-view.component.html',
  styleUrl: './receipt-view.component.css'
})
export class ReceiptViewComponent implements OnInit{
  receipt:  Receipt | undefined;
  totalConsumption: number = 0;
  green: number = 0;
  blue: number = 0;
  red: number = 0;
  status: string = '';
  finishedAt: string = '';

  constructor(private route: ActivatedRoute,
              private router : Router,
              private service: PriceListService) {
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
    const id = Number(this.route.snapshot.paramMap.get('receiptId'));
    console.log(id);

    this.service.getReceipt(id).subscribe({
      next: (data: Receipt) => {
        this.receipt = data;
        if (this.receipt !== undefined){
          this.status = this.receipt.paid ? 'Paid' : 'Not paid';
          this.totalConsumption = this.receipt.blueZoneConsumption + this.receipt.greenZoneConsumption + this.receipt.redZoneConsumption;
          if (this.receipt.paymentDate !== undefined && this.receipt.paymentDate !== null) {
            this.finishedAt = format(new Date(this.receipt.paymentDate), 'dd.MM.yyyy.');
          } else{
            this.finishedAt = 'Unknown';
          }
        }
      },
      error: (_:any) => {
        console.log("Error with fetching receipt!")
      }
    });


  }

  pay():void {
    if (this.receipt !== undefined && !this.receipt?.paid){
      this.router.navigate(['receipt', this.receipt.id, 'payment']);
    }
  }
}
