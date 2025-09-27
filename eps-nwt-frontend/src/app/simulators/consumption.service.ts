import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ConsumptionService {

  private apiUrl = '/api/consumption';

  constructor(private http: HttpClient) { }

  getCitiesFromInflux(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/cities`);
  }

  getConsumption(city: string, timeRange: string): Observable<number> {
    return this.http.get<number> (`${this.apiUrl}/${city}/${timeRange}`);
  }

  getGraphData(name: string, timeRange: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/graph/${name}/${timeRange}`);
  }

  getHouseholdConsumption(householdId: number, timeRange: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/household/${householdId}/${timeRange}`);
  }

  getGraphHouseholdConsumption(householdId:number, timeRange: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/household/graph/${householdId}/${timeRange}`);
  }
}
