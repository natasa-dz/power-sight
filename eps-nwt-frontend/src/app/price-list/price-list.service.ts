import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {PriceList} from "../model/price-list";

@Injectable({
  providedIn: 'root'
})
export class PriceListService {

  private apiUrl = 'http://localhost:8080/price-list';

  constructor(private http: HttpClient) { }

  addPriceList(priceList: PriceList) {
    return this.http.post<string>(`${this.apiUrl}/create`, priceList);
  }
}
