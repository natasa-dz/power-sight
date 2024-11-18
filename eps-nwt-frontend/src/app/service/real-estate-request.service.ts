import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {RealEstateRequestDTO} from "../model/create-real-estate-request-dto.model";
import {CityMunicipality} from "../model/city-municipality";
import {Observable} from "rxjs";
import {AllRealEstateRequestsDto} from "../model/all-real-estate-requests-dto";
import {RealEstateRequest} from "../model/real-estate-request.model";
import {FinishRealEstateRequestDTO} from "../model/finish-real-estate-request-dto";

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

  getAllRequestsForAdmin() : Observable<AllRealEstateRequestsDto[]> {
    return this.http.get<AllRealEstateRequestsDto[]>(`${this.apiUrl}/admin/requests`)
  }

  getRequestForAdmin(id: number) : Observable<RealEstateRequest> {
    return this.http.get<RealEstateRequest>(`${this.apiUrl}/admin/request/${id}`)
  }

  getImagesByRealEstateId(realEstateId: number) {
    return this.http.get<string[]>(`${this.apiUrl}/images/${realEstateId}`, {
      responseType: 'json'
    });
  }

  getDocumentBytes(filePath: string) {
    return this.http.post(this.apiUrl + '/docs', filePath, {
      responseType: 'arraybuffer'
    });
  }

  finishRequest(requestId: number, finishedRequest: FinishRealEstateRequestDTO) {
    return this.http.put<string>(`${this.apiUrl}/admin/finish/${requestId}`, finishedRequest);
  }
}
