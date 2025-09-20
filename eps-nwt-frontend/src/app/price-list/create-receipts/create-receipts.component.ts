import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {PriceListService} from "../price-list.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {BaseModule} from "../../base/base.module";

@Component({
  selector: 'app-create-receipts',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    ReactiveFormsModule,
    RouterLink,
    NgForOf,
    BaseModule
  ],
  templateUrl: './create-receipts.component.html',
  styleUrl: './create-receipts.component.css'
})
export class CreateReceiptsComponent implements OnInit {
  receiptForm!: FormGroup;
  years: number[] = [];
  selectedMonth: string = ""
  selectedYear: number = 0

  constructor(
    private fb: FormBuilder,
    private priceListService: PriceListService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    for (let i = 2000; i < 2101; i++) {
      this.years.push(i);
    }
  }

  ngOnInit(): void {
    this.receiptForm = this.fb.group({
      month: ['', [Validators.required]],
      year: ['', Validators.required]
    });
  }

  submit() {
    if (this.selectedMonth != "" && this.selectedYear > 1999 && this.selectedYear < 2101) {
      this.priceListService.generateReceipts(this.selectedMonth, this.selectedYear).subscribe({
        next: (response) => {
          this.showSnackbar(response);
        },
        error: (err) => {
          this.showSnackbar(err.error);
        }
      });
    } else {
      this.showSnackbar('Please select all the options');
    }
  }

  showSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }

  onYearChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.selectedYear = Number(target.value);
    }
  }

  onMonthChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.selectedMonth = target.value;
    }
  }
}
