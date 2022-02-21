package com.apress.kubdev.news.feeds;

import static io.restassured.RestAssured.given;

import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FeedsControllerTest {

	@Test
	void failCreateFeedItemWithoutBody() {
		given()
			.when().post("/news")
			.then()
				.statusCode(415);
	}
	
	@Test
	void failCreateFeedItemWithInvalidUrl() {
		FeedItem item = new FeedItem();
		item.setDescription("desc");
		item.setLink("example.com");
		item.setTitle("myfeed");
		given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(item)
			.when().post("/news")
			.then()
				.statusCode(400);
	}

	@Test
	@Transactional
	void createFeedItem() {
		FeedItem item = new FeedItem();
		item.setDescription("desc");
		item.setLink("http://example.com");
		item.setTitle("myfeed");
		given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(item)
			.when().post("/news")
			.then()
				.statusCode(201);
	}
	
	
	@Test
	@Transactional
	void createDuplicateFeedItem() {
		FeedItem item = new FeedItem();
		item.setDescription("desc");
		item.setLink("http://example2.com");
		item.setTitle("myfeed2");
		given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(item)
			.when().post("/news")
			.then()
				.statusCode(201);
		given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(item)
			.when().post("/news")
			.then()
				.statusCode(409);
	}
}
