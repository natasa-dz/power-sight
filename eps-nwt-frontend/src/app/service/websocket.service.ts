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
  private dataSubject = new Subject<any>();
  public data$ = this.dataSubject.asObservable();

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

  disconnect(): void {
    if (this.client) {
      this.client.disconnect(() => {
        this.isConnected = false;
      });
    }
  }
}
