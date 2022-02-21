import { Component, OnInit } from '@angular/core';
import { ScraperService } from '../scraper.service';

@Component({
  selector: 'app-scraperconfig',
  templateUrl: './scraperconfig.component.html',
  styleUrls: ['./scraperconfig.component.css']
})
export class ScraperconfigComponent implements OnInit {

  feedScraperUrl = [""];

  constructor(private scraperService: ScraperService) { }

  ngOnInit(): void {
  }

  scrape(): void {
 	console.log(this.feedScraperUrl);	
	this.scraperService.scrape(this.feedScraperUrl).subscribe(scraping => console.log(scraping));
  }

  addUrl(): void {
	
  }

}
