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

  private serverUrl = 'http://localhost:8080/socket'; // This is the base URL

  connect(): void {
    if (this.isConnected) return;

    const accessToken: any = localStorage.getItem('user');
    const simulatorId: any = localStorage.getItem('simulator-id'); // Get simulator ID from local storage

    // Append simulator ID as query parameter
    const socketUrl = `${this.serverUrl}?simulatorId=${simulatorId}`; // Using query parameter here
    // @ts-ignore
    const socket = new SockJS(socketUrl, null, { withCredentials: true }); // Enable credentials
    this.client = over(socket);

    this.client.connect({ Authorization: `Bearer ${accessToken}` }, (frame) => {
      console.log('Connected: ' + frame);
      this.isConnected = true;
    }, (error) => {
      console.error('WebSocket connection error', error);
    });
  }

  subscribe(topic: string): void {
    if (!this.isConnected || !this.client) return;

    this.client.subscribe(topic, (message: Message) => {
      const body = JSON.parse(message.body);
      this.dataSubject.next(body);
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
