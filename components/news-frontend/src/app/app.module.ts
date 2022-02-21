import { NgModule, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { HttpClientModule } from '@angular/common/http';
import { NewsService } from './news.service';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MapComponent } from './map/map.component';
import { SettingsHttpService } from './settings/settings.http.service';
import { ScraperconfigComponent } from './scraperconfig/scraperconfig.component';
import { FormsModule } from '@angular/forms';

export function app_Init(settingsHttpService: SettingsHttpService) {
  return () => settingsHttpService.initializeApp();
}

@NgModule({
  declarations: [
    AppComponent,
    MapComponent,
    ScraperconfigComponent
  ],
  imports: [
    BrowserModule,
	  HttpClientModule,
    AppRoutingModule,
	FormsModule
  ],
  providers: [
	  NewsService,
    { provide: APP_INITIALIZER, useFactory: app_Init, deps: [SettingsHttpService], multi: true }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
