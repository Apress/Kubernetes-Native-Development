package com.apress.kubdev.news.extractor;

import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apress.kubdev.news.core.TextAnalyzerService;
import com.apress.kubdev.news.core.domain.model.Location;
import com.apress.kubdev.news.core.domain.model.News;

@ApplicationScoped
public class LocationExtractorAdapter implements TextAnalyzerService{
	private static final Logger LOGGER = LoggerFactory.getLogger(LocationExtractorAdapter.class);
	
	@Inject
	@RestClient
	LocationExtractorClient extractorClient;
	
	private long feedsWithLocation;
	private long feedsAnalyzed;
	
	@Override
	public Optional<Location> getLocation(News news) {
		LOGGER.info("Analyzing text '{}'", news.getDescription());
		Map<String, AnalysisResult> results = extractorClient.analyzeText(news.getDescription());
		if (!results.isEmpty()) {
			return extractFirstLocation(results, 1);
		} else {
			return Optional.empty();
		}
	}

	private Optional<Location> extractFirstLocation(Map<String, AnalysisResult> results, int offset) {
		String key = String.valueOf(offset);
		feedsAnalyzed++;
		if (results.containsKey(key)) {
			AnalysisResult firstResult = results.get(key);
			if (firstResult.getLatitude() != null && firstResult.getLongitude() != null) {
				LOGGER.info("Analysis result is '{}'", firstResult.getLocationName());
				Location location = Location.builder()
						.withLatitude(firstResult.getLatitude())
						.withLongitude(firstResult.getLongitude())
						.build();
				feedsWithLocation++;
				return Optional.of(location);
			} else {
				LOGGER.info("No location data in result. Looking for next key {}", offset + 1);
				return extractFirstLocation(results, ++ offset);
			}
		} else {
			LOGGER.info("No key {} in results. Stopping extraction.", key);
			return Optional.empty();
		}
	}
	
	@Gauge(absolute = true, name = "feedsWithLocation", unit = MetricUnits.NONE, description = "For how many feeds the analyzer has found location data.")
    public Long feedsWithLocation() {
        return feedsWithLocation;
    }
	
	@Gauge(absolute = true, name = "feedsAnalyzed", unit = MetricUnits.NONE, description = "How many feeds have been analyzed.")
    public Long feedsAnalyzed() {
        return feedsAnalyzed;
    }

}
