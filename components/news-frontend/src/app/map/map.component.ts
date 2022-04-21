import { Component, AfterViewInit } from '@angular/core';
import * as L from 'leaflet';
import { NewsService } from '../news.service';

const iconRetinaUrl = 'assets/marker-icon-2x.png';
const iconUrl = 'assets/marker-icon.png';
const shadowUrl = 'assets/marker-shadow.png';
const iconDefault = L.icon({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements AfterViewInit {
  private map: L.Map;

  private initMap(): void {
    this.map = L.map('map', {
      center: [ 39.8282, -18.5795 ],
      zoom: 3
    });

	const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    });

    tiles.addTo(this.map);
    var self = this;

    var southWest = L.latLng(-8.501445495750618, -33.232239902019504),
	        northEast = L.latLng(13.354130902170063, -19.0806285738945),
	        bounds = L.latLngBounds(southWest, northEast);

    L.rectangle(bounds, {color: "black", weight: 3}).addTo(this.map);

    var marker = L.marker([6.354130902170063, -30.232239902019504], { opacity: 0.0 }); //opacity may be set to zero
    marker.bindTooltip("No location found :)", {permanent: true, className: "unlocated", offset: [0, 0] });
    marker.addTo(this.map);

	this.map.on('dragend', function(e) {
   		 self.newsService.drawMarkersForNews(self.map);
	});
	this.map.on('zoomend', function(e) {
   		 self.newsService.drawMarkersForNews(self.map);
	});
  }
  constructor(private newsService: NewsService) { 

  }

  ngAfterViewInit(): void { 
     this.initMap();
	 this.newsService.drawMarkersForNews(this.map);
  }
}
