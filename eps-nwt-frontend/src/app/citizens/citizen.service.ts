import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Page} from "../model/page.model";
import {CitizenSearchDto} from "../model/citizen-search-dto";

@Injectable({
  providedIn: 'root'
})
export class CitizenService {

  private apiUrl = 'http://localhost:8080/citizen';

  constructor(private http: HttpClient) { }

  search(username: string = '', page: number = 0, size: number = 10): Observable<Page<CitizenSearchDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('username', username);

    return this.http.get<Page<CitizenSearchDto>>(`${this.apiUrl}/search`, { params });
  }
}
