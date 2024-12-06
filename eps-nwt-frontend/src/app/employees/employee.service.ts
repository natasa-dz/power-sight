import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Page} from "../model/page.model";
import {HouseholdSearchDTO} from "../model/household-search-dto.model";
import {EmployeeSearchDto} from "../model/employee-search-dto.model";

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  private apiUrl = 'http://localhost:8080/employee';

  constructor(private http: HttpClient) { }

  search(name: string, jmbg?: string, page: number = 0, size: number = 10) {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (jmbg !== undefined && jmbg !== null) {
      params = params.set('jmbg', jmbg.toString());
    }

    const url = `${this.apiUrl}/search/${name}`;
    return this.http.get<Page<EmployeeSearchDto>>(url, { params });
  }
}
