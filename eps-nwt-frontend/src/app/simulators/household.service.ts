import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {HouseholdSearchDTO} from "../model/household-search-dto.model";
import {Page} from "../model/page.model";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class HouseholdService {

  private apiUrl = 'http://localhost:8080/household/search';  // Adjust to your backend URL

  constructor(private http: HttpClient) {}

  search(municipality: string, address: string, apartmentNumber?: number, page: number = 0, size: number = 10): Observable<Page<HouseholdSearchDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (apartmentNumber !== undefined && apartmentNumber !== null) {
      params = params.set('apartmentNumber', apartmentNumber.toString());
    }

    const url = `${this.apiUrl}/${municipality}/${address}`;
    return this.http.get<Page<HouseholdSearchDTO>>(url, { params });
  }
}
