import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {HttpParams} from "@angular/common/http";
import { SettingsService } from "./settings/settings.service";
import * as L from 'leaflet';


@Injectable({
  providedIn: 'root'
})
export class NewsService {
  //capitals: string = '/assets/data/usa-capitals.geojson';
  
  constructor(private http: HttpClient, private settingService: SettingsService) { }

  markerPopup(data: any): string {
    return `` +
      `<div>Title: ${ data.title }</div>` +
      `<div>Link: <a target="_blank" href="${ data.link }">${ data.link }</a></div>` +
      `<div>Description: ${ data.description }</div>`
  }


  drawMarkersForNews(map: L.Map): void {
    const bounds = map.getBounds()
	const options = { params: new HttpParams()
		.set('sw.lat', bounds.getSouthWest().lat)
		.set('sw.lng', bounds.getSouthWest().lng)
		.set('ne.lat', bounds.getNorthEast().lat)
		.set('ne.lng', bounds.getNorthEast().lng) };
	//this.http.get(this.capitals).subscribe((res: any) => {
    this.http.get(this.settingService.settings.apiUrl + "/news", options).subscribe((res: any) => {
      for (const c of res) {
        const lon = c.location.longitude;
        const lat = c.location.latitude;
        const marker = L.marker([lat, lon]);
		
        marker.addTo(map);
		marker.bindPopup( this.markerPopup(c));
      }
    });
  }
}
