import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {HouseholdSearchDTO} from "../model/household-search-dto.model";
import {Page} from "../model/page.model";
import {Observable} from "rxjs";
import {Household} from "../model/household.model";
import {ViewHouseholdDto} from "../model/view-household-dto.model";

@Injectable({
  providedIn: 'root'
})
export class HouseholdService {

  private apiUrl = 'http://localhost:8080/household';  // Adjust to your backend URL

  constructor(private http: HttpClient) {}

  search(municipality: string, address: string, apartmentNumber?: number, page: number = 0, size: number = 10): Observable<Page<HouseholdSearchDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (apartmentNumber !== undefined && apartmentNumber !== null) {
      params = params.set('apartmentNumber', apartmentNumber.toString());
    }

    const url = `${this.apiUrl}/search/${municipality}/${address}`;
    return this.http.get<Page<HouseholdSearchDTO>>(url, { params });
  }

  findById(id: number): Observable<ViewHouseholdDto> {
    return this.http.get<ViewHouseholdDto>(`${this.apiUrl}/find-by-id/${id}`);
  }

  getAvailability(name: string, timeRange: string): Observable<Map<string, string>> {
    return this.http.get<Map<string, string>>(`${this.apiUrl}/availability/${name}/${timeRange}`);
  }

  getGraphData(name: string, timeRange: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/graph/${name}/${timeRange}`);
  }

}
