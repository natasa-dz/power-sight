import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {RealEstateRequestDTO} from "../model/create-real-estate-request-dto.model";
import {CityMunicipality} from "../model/city-municipality";
import {Observable} from "rxjs";
import {AllRealEstateRequestsDto} from "../model/all-real-estate-requests-dto";

@Injectable({
  providedIn: 'root'
})
export class RealEstateRequestService {

  private apiUrl = 'http://localhost:8080/real-estate-request';

  private headers = new HttpHeaders({
    'Content-Type': 'application/json',
    skip: 'true',
  });

  constructor(private http: HttpClient) { }

  createRequest(realEstateRequest: RealEstateRequestDTO, images: File[], documentation: File[]) {
    const data : FormData = new FormData();
    data.append('realEstateRequest', new Blob([JSON.stringify(realEstateRequest)], { type: 'application/json' }));
    for (let image of images){
      data.append("images", image);
    }
    for (let doc of documentation){
      data.append("documentation", doc);
    }
    return this.http.post<string>(`${this.apiUrl}/registration`, data);
  }

  getCitiesWithMunicipalities(): Observable<CityMunicipality> {
    return this.http.get<CityMunicipality>(this.apiUrl);
  }

  getAllRequestsForOwner(ownerId: number) : Observable<AllRealEstateRequestsDto[]> {
    return this.http.get<AllRealEstateRequestsDto[]>(`${this.apiUrl}/${ownerId}/all`)
  }
}
