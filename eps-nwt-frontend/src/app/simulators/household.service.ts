import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {HouseholdSearchDTO} from "../model/household-search-dto.model";
import {Page} from "../model/page.model";
import {Observable} from "rxjs";
import {Household} from "../model/household.model";
import {ViewHouseholdDto} from "../model/view-household-dto.model";
import {HouseholdDto} from "../model/householdDTO";

@Injectable({
  providedIn: 'root'
})
export class HouseholdService {

  private apiUrl = 'http://localhost:8080/household';  // Adjust to your backend URL
  private ownershipUrl = 'http://localhost:8080/ownership-requests';  // Adjust to your backend URL

  constructor(private http: HttpClient) {}

  getHouseholdsWithoutOwner(page: number, size: number): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    const url =`${this.apiUrl}/no-owner`;
    return this.http.get<Page<HouseholdDto>>(url, {params});
  }

  createOwnershipRequest(formData: FormData): Observable<any> {
    const url =`${this.apiUrl}/no-owner`;
    return this.http.post(url, formData);
  }

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

  getLatestValue(name: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/current/${name}`);
  }

  submitOwnershipRequest(
    userId: string,
    householdId: number,
    files: File[]
  ): Observable<any> {
    const formData = new FormData();
    formData.append('userId', userId);
    formData.append('householdId', householdId.toString());

    files.forEach(file => {
      formData.append('files', file);  // Remove file.name
    });

    // Log formData keys and values using a manual loop
    formData.forEach((value, key) => {
      console.log(`FormData - ${key}:`, value);
    });

    return this.http.post(`${this.ownershipUrl}/requestOwnership`, formData, { responseType: 'text' });
  }

  getUserOwnershipRequests(userId: string): Observable<any> {
    return this.http.get(`${this.ownershipUrl}/${userId}`);
  }

  getPendingRequests(): Observable<any> {
    return this.http.get(`${this.ownershipUrl}/pending`);
  }

  processRequest(requestId: number, approved: boolean, reason?: string ): Observable<any> {
    const payload = { requestId, approved, reason };
    return this.http.post(`${this.ownershipUrl}/process`, payload, { responseType: 'text' });
  }


}
