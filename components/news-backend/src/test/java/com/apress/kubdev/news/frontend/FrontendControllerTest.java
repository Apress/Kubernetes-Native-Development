package com.apress.kubdev.news.frontend;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import com.apress.kubdev.news.core.NewsRepository;
import com.apress.kubdev.news.core.domain.model.Location;
import com.apress.kubdev.news.core.domain.model.News;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;

@QuarkusTest
class FrontendControllerTest {
	
	
	@Inject
	NewsRepository repository;
	
	private News createTestFeed(double latitude, double longitude, String link) {
		Location location = Location.builder()
				.withLatitude(latitude)
				.withLongitude(longitude)
				.build();
		try {
			return News.builder()
					.withDescription("Test")
					.withLink(new URL(link))
					.withLocation(Optional.of(location))
					.build();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e);
			return null;
		}
		
	}
	
	@Test
	void getFeedsByBounds() {
		News testFeed = createTestFeed(50.1186, 8.6696, "http://example3.com");
		repository.create(testFeed);
		RequestSpecBuilder specBuilder = new RequestSpecBuilder();
		specBuilder.addParam("sw.lat", "54.716749");
		specBuilder.addParam("sw.lng", "5.389149");
		specBuilder.addParam("ne.lat", "47.634028");
		specBuilder.addParam("ne.lng", "15.320665");
		given()
			.spec(specBuilder.build())
			.contentType(MediaType.APPLICATION_JSON)
			.when().get("/news")
			.then()
				.statusCode(200);
		//TODO Check result
	}
	
	@Test
	void getFeedsNoArgs() {
		News testFeed = createTestFeed(50.1186, 8.6696, "http://example4.com");
		repository.create(testFeed);
		given()
			.contentType(MediaType.APPLICATION_JSON)
			.when().get("/news")
			.then()
				.statusCode(200);
		//TODO Check result
	}
	
}
