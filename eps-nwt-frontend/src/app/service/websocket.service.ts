import { Injectable } from '@angular/core';
import { Client, Message, over } from 'stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  client: Client | null = null;
  isConnected = false;
  isConnectedCity : Map<string, boolean> = new Map();
  isConnectedHouse : Map<number, boolean> = new Map();
  private dataSubjectHouse = new Subject<any>();

  private dataSubject = new Subject<any>();
  private dataSubjectCity = new Subject<any>();
  public data$ = this.dataSubject.asObservable();
  public cityData$ = this.dataSubjectCity.asObservable();

  public houseData$ = this.dataSubjectCity.asObservable();

  private serverUrl = 'http://localhost:8080/socket';

  connect(): void {
    if (this.isConnected) return;

    const accessToken: any = localStorage.getItem('user');
    const simulatorId: any = localStorage.getItem('simulator-id');
    const socket = new SockJS(this.serverUrl);
    this.client = over(socket);

    this.client.connect({ Authorization: `Bearer ${accessToken}` }, (frame) => {
      this.isConnected = true;
      // @ts-ignore
      this.client.subscribe('/data/graph/' + simulatorId, (messageOutput) => {
        const data = JSON.parse(messageOutput.body);
        this.dataSubject.next(data);
      });
    }, (error) => {
      console.error('WebSocket connection error', error);
    });
  }

  connectCity(city : string): void {
    if (this.isConnectedCity.get(city)) return;

    const accessToken: any = localStorage.getItem('user');
    const socket = new SockJS(this.serverUrl);
    this.client = over(socket);

    this.client.connect({ Authorization: `Bearer ${accessToken}` }, (frame) => {
      this.isConnectedCity.set(city, true);
      // @ts-ignore
      this.client.subscribe('/data/graph/' + city, (messageOutput) => {
        const cityData = JSON.parse(messageOutput.body);
        /*console.log(cityData)
        console.log(city)*/
        this.dataSubjectCity.next(cityData);
      });
    }, (error) => {
      console.error('WebSocket connection error', error);
    });
  }

  disconnect(): void {
    if (this.client) {
      this.client.disconnect(() => {
        this.isConnected = false;
      });
    }
  }

  disconnectCity(city : string): void {
    if (this.client) {
      this.client.disconnect(() => {
        this.isConnectedCity.set(city, false);
        this.dataSubjectCity = new Subject<any>();
        this.cityData$ = this.dataSubjectCity.asObservable();
      });
    }
  }

  connectHouse(houseId:number){
    if (this.isConnectedHouse.get(houseId)) return;

    const accessToken: any = localStorage.getItem('user');
    const socket = new SockJS(this.serverUrl);
    this.client = over(socket);

    this.client.connect({ Authorization: `Bearer ${accessToken}` }, (frame) => {
      this.isConnectedHouse.set(houseId, true);
      // @ts-ignore
      this.client.subscribe('/data/household/graph/' + houseId, (messageOutput) => {
        const houseData = JSON.parse(messageOutput.body);
        console.log("Print from connectHouse: ")
        console.log(houseData)
        console.log(houseId)
        console.log("--------------------------------------")
        this.dataSubjectHouse.next(houseData);
      });
    }, (error) => {
      console.error('WebSocket connection error', error);
    });
  }

  disconnectHouse(houseId:number){
    if (this.client) {
      this.client.disconnect(() => {
        this.isConnectedHouse.set(houseId, false);
        this.dataSubjectHouse = new Subject<any>();
        this.houseData$ = this.dataSubjectHouse.asObservable();
      });
    }
  }
}
