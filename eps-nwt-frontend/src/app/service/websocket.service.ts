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
      this.client.subscribe('/data/graph/' + simulatorId, function(messageOutput) {
        console.log("STIGLE PORUKE U SERVIS" + messageOutput.body);
      });
    }, (error) => {
      console.error('WebSocket connection error', error);
    });
  }

  subscribe(simulatorId: string): void {
    if (!this.isConnected || !this.client) return;

    const topic = `/data/graph/${simulatorId}`;
    console.log(`Subscribing to topic: ${topic}`);

    this.client.subscribe(topic, (message: Message) => {
      console.log('Received message:', message);
      try {
        const body = JSON.parse(message.body);
        console.log('Parsed data:', body);
        this.dataSubject.next(body.data); // Push data to the subject
      } catch (error) {
        console.error('Error parsing message body:', error);
      }
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
