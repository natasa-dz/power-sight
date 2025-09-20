import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {BaseModule} from "../../base/base.module";
import {NgIf} from "@angular/common";
import {PriceListService} from "../price-list.service";
@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [
    FormsModule,
    BaseModule,
    NgIf
  ],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.css'
})
export class PaymentComponent implements OnInit{
  payment = {
    cardNumber: '',
    expiryDate: '',
    cvv: '',
    cardholderName: ''
  };
  isLoading = false;
  receiptId = 0;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private service: PriceListService) {}

  ngOnInit() {
    this.receiptId = Number(this.route.snapshot.paramMap.get('receiptId'));
  }

  formatCardNumber(): void {
    this.payment.cardNumber = this.payment.cardNumber
      .replace(/\D/g, '')
      .substring(0, 16)
      .replace(/(.{4})/g, '$1 ')
      .trim();
  }

  formatExpiryDate(): void {
    this.payment.expiryDate = this.payment.expiryDate
      .replace(/\D/g, '')
      .substring(0, 4);

    if (this.payment.expiryDate.length >= 3) {
      this.payment.expiryDate = this.payment.expiryDate.replace(/(\d{2})(\d{1,2})/, '$1/$2');
    }
  }

  submitPayment(): void {
    this.isLoading = true;

    this.service.pay(this.receiptId).subscribe({
      next: (data: string) => {
        console.log(data)
      },
      error: (err:any) => {
        console.log("Error with payment process: ", err)
      }
    });

    setTimeout(() => {
      this.isLoading = false;
      alert('Payment successful!');
      this.router.navigate(['/receipts']);
    }, 1500);
  }
}
