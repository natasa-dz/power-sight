import {AfterViewInit, Component} from '@angular/core';
import * as L from "leaflet";

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [],
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})
export class MapComponent implements AfterViewInit{
  map : any;

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
  }


  ngAfterViewInit(): void {
    this.initMap();
  }
}
