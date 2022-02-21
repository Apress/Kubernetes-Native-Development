import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse,HttpHeaders  } from '@angular/common/http';
import { SettingsService } from "./settings/settings.service";
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ScraperService {

  constructor(private http: HttpClient, private settingService: SettingsService) { }

  scrape(url: string[]): Observable<string> {
	console.log("Sending post to " + this.settingService.settings.apiUrl + "/scraper");
	var options = {headers : new HttpHeaders({ 'Content-Type': 'application/json' })};
	return this.http.post<string>(this.settingService.settings.apiUrl + "/scraper", url, options).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
	console.error(
      `Backend returned code ${error.status}, body was: `, error.error);
 	
	return throwError('Something bad happened; please try again later.');
  }
}
