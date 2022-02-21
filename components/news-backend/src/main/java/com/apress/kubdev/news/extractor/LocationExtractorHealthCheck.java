package com.apress.kubdev.news.extractor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Liveness
@Readiness
@ApplicationScoped
public class LocationExtractorHealthCheck implements HealthCheck {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocationExtractorHealthCheck.class);
	@ConfigProperty(name = "nlp.up", defaultValue = "false")
	private boolean databaseUp;
	
	@Inject
	@RestClient
	LocationExtractorClient nlpClient;

	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("NLP connection health check");
		try {
			healthCheck();
			responseBuilder.up();
		} catch (Exception e) {
			LOGGER.error("Error in nlp healthcheck", e);
			responseBuilder.down()
			.withData("Message", e.getMessage());
			
		}

		return responseBuilder.build();
	}

	private void healthCheck() {
		nlpClient.analyzeText("");
	}
}

