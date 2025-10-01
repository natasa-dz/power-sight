import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Page} from "../model/page.model";
import {CitizenSearchDto} from "../model/citizen-search-dto";
import {HouseholdAccessDto} from "../model/household-access-dto.model";

@Injectable({
  providedIn: 'root'
})
export class CitizenService {

  private apiUrl = '/api/citizen';
  private householdApiUrl = '/api/household';

  constructor(private http: HttpClient) { }

  search(username: string = '', page: number = 0, size: number = 5): Observable<Page<CitizenSearchDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('username', username);

    return this.http.get<Page<CitizenSearchDto>>(`${this.apiUrl}/search`, { params });
  }

  getHouseholdsForOwner(ownerId : number) : Observable<HouseholdAccessDto[]>{
    return this.http.get<HouseholdAccessDto[]>(`${this.householdApiUrl}/getForOwner/${ownerId}`);

  }

  allowAccess(householdId: number, selectedIds: number[]) : Observable<string> {
    return this.http.put<string>(`${this.householdApiUrl}/allow-access/${householdId}`, selectedIds);
  }

  getProfileImage(userId: number) {
    return this.http.get(`${this.apiUrl}/image/${userId}`,{ responseType: 'text' });
  }
}
