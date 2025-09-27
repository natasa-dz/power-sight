import {AfterViewInit, Component, EventEmitter, Output} from '@angular/core';
import * as L from "leaflet";
import {HttpClient} from "@angular/common/http";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [],
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})
export class MapComponent implements AfterViewInit{
  map : L.Map | undefined;

  @Output() addressChange = new EventEmitter<string>();

  constructor(private http: HttpClient,
              private snackBar: MatSnackBar) {}

  private initMap(): void {
    // Novi Sad
    this.map = L.map('map', {
      center: [ 45.267136, 19.833549 ],
      zoom: 12
    });


    const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    });

    tiles.addTo(this.map);

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;
      this.http
        .get(`/nominatim/reverse?format=jsonv2&lat=${lat}&lon=${lng}`)
        .subscribe((data: any) => {
          const road = data?.address?.road || 'Unknown road';
          if (road === 'Unknown road') {
            this.showSnackbar("Unknown address")
            return
          }
          const number = data?.address?.house_number || 'Unknown number';
          const fullAddress = `${road} ${number !== 'Unknown number' ? number : 'bb'}`;
          this.addressChange.emit(fullAddress);
          const popup = L.popup()
            .setLatLng([lat, lng])
            .setContent(`<p>Location:</p><p>${road} ${number !== 'Unknown number' ? number : 'bb'}</p`)
            .openOn(this.map!);
        });
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  showSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }
}
