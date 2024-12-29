import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PriceList} from "../model/price-list";
import {Observable} from "rxjs";
import {Receipt} from "../model/receipt.model";

@Injectable({
  providedIn: 'root'
})
export class PriceListService {

  private apiUrl = 'http://localhost:8080/price-list';
  private receiptUrl = 'http://localhost:8080/receipts';

  constructor(private http: HttpClient) { }

  addPriceList(priceList: PriceList) {
    return this.http.post<string>(`${this.apiUrl}/create`, priceList);
  }

  generateReceipts(month: string, year: number) {
    return this.http.post<string>(`${this.receiptUrl}/create/${month}/${year}`, null);
  }

  getAllReceiptsForOwner(userId: number) : Observable<Receipt[]> {
    return this.http.get<Receipt[]>(`${this.receiptUrl}/all-for-owner/${userId}`);
  }
}
