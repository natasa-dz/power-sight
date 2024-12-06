import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Page} from "../model/page.model";
import {HouseholdSearchDTO} from "../model/household-search-dto.model";
import {EmployeeSearchDto} from "../model/employee-search-dto.model";
import {Observable} from "rxjs";
import {ViewHouseholdDto} from "../model/view-household-dto.model";
import {EmployeeViewDto} from "../model/view-employee-dto.model";

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  private apiUrl = 'http://localhost:8080/employee';

  constructor(private http: HttpClient) { }

  search(username: string, page: number = 0, size: number = 10) {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    const url = `${this.apiUrl}/search/${username}`;
    return this.http.get<Page<EmployeeSearchDto>>(url, { params });
  }

  findById(id: number): Observable<EmployeeViewDto> {
    return this.http.get<EmployeeViewDto>(`${this.apiUrl}/find-by-id/${id}`);
  }

  getProfileImage(path: string) {
    return this.http.post<string>(`${this.apiUrl}/image`, path, { responseType: 'text' as 'json' });
  }

  suspend(id: number): Observable<Boolean> {
    return this.http.put<Boolean>(`${this.apiUrl}/suspend/${id}`, null);
  }
}
