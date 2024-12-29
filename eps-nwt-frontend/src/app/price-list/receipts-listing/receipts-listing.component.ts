import {Component, OnInit} from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ReceiptsCardComponent} from "../receipts-card/receipts-card.component";
import {Receipt} from "../../model/receipt.model";
import {PriceListService} from "../price-list.service";

@Component({
  selector: 'app-receipts-listing',
  standalone: true,
  imports: [
    BaseModule,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    ReceiptsCardComponent,
    FormsModule
  ],
  templateUrl: './receipts-listing.component.html',
  styleUrl: './receipts-listing.component.css'
})
export class ReceiptsListingComponent implements OnInit{
  receipts : Receipt[] = [];
  sortType: string = "createdAtAsc";
  filterParams = {
    status: null,
    address: '',
    createdFromDate: null,
    createdToDate: null,
    finishedFromDate: null,
    finishedToDate: null,
    priceFrom: 0,
    priceTo: 999999999,
    consumptionFrom: 0,
    consumptionTo: 999999999
  };
  filteredReceipts: Receipt[] = [];
  showFilters = false;

  constructor(private service : PriceListService) {
  }

  ngOnInit(): void {
    let loggedId = Number(localStorage.getItem('userId'));
    if (loggedId != undefined){
      this.service.getAllReceiptsForOwner(loggedId).subscribe({
        next: (data: Receipt[]) => {
          this.receipts = data
          this.filteredReceipts = [...this.receipts];
          this.applyFilters();
        },
        error: (_:any) => {
          console.log("Error with all receipts!")
        }
      });
    }
  }

  getTime(date: Date | null) {
    return date != null ? new Date(date).getTime() : 0;
  }

  getMonthNumber(month: string): number {
    const months = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
    ];
    return months.indexOf(month) + 1;
  }

  sortReceipts(): void {
    switch (this.sortType) {
      case 'createdAtAsc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return (a.year * 100 + this.getMonthNumber(a.month)) -
            (b.year * 100 + this.getMonthNumber(b.month));
        });
        break;
      case 'createdAtDesc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return (b.year * 100 + this.getMonthNumber(b.month)) -
            (a.year * 100 + this.getMonthNumber(a.month));
        });
        break;
      case 'finishedAtAsc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return this.getTime(a.paymentDate) - this.getTime(b.paymentDate);
        });
        break;
      case 'finishedAtDesc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return this.getTime(b.paymentDate) - this.getTime(a.paymentDate);
        });
        break;
      case 'priceAsc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return a.price - b.price;
        });
        break;
      case 'priceDesc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return b.price - a.price;
        });
        break;
      case 'consumptionAsc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return (a.greenZoneConsumption + a.blueZoneConsumption + a.redZoneConsumption) -
            (b.greenZoneConsumption + b.blueZoneConsumption + b.redZoneConsumption);
        });
        break;
      case 'consumptionDesc':
        this.filteredReceipts = this.receipts.sort((a, b) => {
          return (b.greenZoneConsumption + b.blueZoneConsumption + b.redZoneConsumption) -
            (a.greenZoneConsumption + a.blueZoneConsumption + a.redZoneConsumption);
        });
        break;
    }
  }


  applyFilters(): void {
    this.filteredReceipts = this.receipts.filter((receipt) => {
      // Status filter
      if (this.filterParams.status === 'Paid' && !receipt.isPaid) {
        return false;
      }
      if (this.filterParams.status === 'Not Paid' && receipt.isPaid) {
        return false;
      }

      // Address filter
      if (
        this.filterParams.address &&
        receipt.householdAddress &&
        !receipt.householdAddress.toLowerCase().includes(this.filterParams.address.toLowerCase())
      ) {
        return false;
      }

      // Date filters for created date (month + year)
      const receiptYearMonth = receipt.year * 100 + this.getMonthNumber(receipt.month);
      const fromYearMonth =
        this.filterParams.createdFromDate
          ? new Date(this.filterParams.createdFromDate).getFullYear() * 100 +
          new Date(this.filterParams.createdFromDate).getMonth() + 1
          : null;
      const toYearMonth =
        this.filterParams.createdToDate
          ? new Date(this.filterParams.createdToDate).getFullYear() * 100 +
          new Date(this.filterParams.createdToDate).getMonth() + 1
          : null;

      if (fromYearMonth && receiptYearMonth < fromYearMonth) {
        return false;
      }

      if (toYearMonth && receiptYearMonth > toYearMonth) {
        return false;
      }

      // Payment date filters
      const paymentDateTime = this.getTime(receipt.paymentDate);
      const fromPaymentDateTime = this.filterParams.finishedFromDate
        ? new Date(this.filterParams.finishedFromDate).getTime()
        : null;
      const toPaymentDateTime = this.filterParams.finishedToDate
        ? new Date(this.filterParams.finishedToDate).getTime()
        : null;

      if (fromPaymentDateTime && (!paymentDateTime || paymentDateTime < fromPaymentDateTime)) {
        return false;
      }

      if (toPaymentDateTime && (!paymentDateTime || paymentDateTime > toPaymentDateTime)) {
        return false;
      }

      // Price filter
      if (this.filterParams.priceFrom && receipt.price < this.filterParams.priceFrom) {
        return false;
      }

      if (this.filterParams.priceTo && receipt.price > this.filterParams.priceTo) {
        return false;
      }

      // Consumption filter
      const totalConsumption =
        receipt.greenZoneConsumption + receipt.blueZoneConsumption + receipt.redZoneConsumption;

      if (this.filterParams.consumptionFrom && totalConsumption < this.filterParams.consumptionFrom) {
        return false;
      }

      if (this.filterParams.consumptionTo && totalConsumption > this.filterParams.consumptionTo) {
        return false;
      }

      return true;
    });
  }



  resetFilters(): void {
    this.filterParams = {
      status: null,
      address: '',
      createdFromDate: null,
      createdToDate: null,
      finishedFromDate: null,
      finishedToDate: null,
      priceFrom: 0,
      priceTo: 999999999,
      consumptionFrom: 0,
      consumptionTo: 999999999
    };
    this.applyFilters();
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

}
