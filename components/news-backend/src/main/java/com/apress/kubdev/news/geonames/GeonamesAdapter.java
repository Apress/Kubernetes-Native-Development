package com.apress.kubdev.news.geonames;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.apress.kubdev.news.core.GeoSearchResult;
import com.apress.kubdev.news.core.GeoSearchService;
import com.apress.kubdev.news.core.domain.model.Location;

@ApplicationScoped
public class GeonamesAdapter implements GeoSearchService{

	
	@Inject
	@RestClient
	GeonamesClient geoNamesClient;
	
	@Override
	public GeoSearchResult search(String name) {
		geoNamesClient.search("Frankfurt", 5, "demo");
		//TODO Pass the results to the expected domain class
		return new GeoSearchResult("Frankfurt", Location.builder().withLatitude(BigDecimal.valueOf(50.1186)).withLongitude(BigDecimal.valueOf(8.6696)).build());
	}

}
