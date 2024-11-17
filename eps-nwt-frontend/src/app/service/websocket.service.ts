import { Injectable } from '@angular/core';
import { Client, Message, over } from 'stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private client: Client | null = null;
  private isConnected = false;
  private dataSubject = new Subject<any>();
  public data$ = this.dataSubject.asObservable();

  private serverUrl = 'http://localhost:8080/socket'; // Base URL

  connect(): void {
    if (this.isConnected) return;

    const accessToken: any = localStorage.getItem('user');
    const simulatorId: any = localStorage.getItem('simulator-id'); // Get simulator ID from local storage
    const socket = new SockJS(this.serverUrl); // Create SockJS instance
    this.client = over(socket);

    this.client.connect({ Authorization: `Bearer ${accessToken}` }, (frame) => {
      // @ts-ignore
      this.client.subscribe('/data/graph/' + simulatorId, (messageOutput) => {
        const data = JSON.parse(messageOutput.body);
        this.dataSubject.next(data);
      });
    }, (error) => {
      console.error('WebSocket connection error', error);
    });
  }

  disconnect(): void {
    if (this.client) {
      this.client.disconnect(() => {
        console.log('Disconnected');
        this.isConnected = false;
      });
    }
  }
}
