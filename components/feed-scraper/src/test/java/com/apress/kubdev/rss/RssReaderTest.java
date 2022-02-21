package com.apress.kubdev.rss;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.PropertiesSource;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

public class RssReaderTest extends CamelTestSupport {

	private static final String FEEDS_URL = "http://localhost:9085";
	private static final int TEST_DURATION_MS = 5000;
	private static final String DELAY_MS = "1000";
	private static final String WIREMOCK_PORT = "9085";
	private Faker faker = new Faker();
	
	@Test
	void test() {
		try (DefaultCamelContext ctx = new DefaultCamelContext()) {
			ctx.addRoutes(createRouteBuilder());
			ctx.getPropertiesComponent().addPropertiesSource(testConfiguration());
			ctx.start();
			WireMockServer wireMockServer = startWireMock(); 
			mockNewsBackend(wireMockServer);
			mockNewsFeed(wireMockServer);
			Thread.sleep(TEST_DURATION_MS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
	}
	
	private void mockNewsBackend(WireMockServer wireMockServer) throws IOException, FeedException {
		wireMockServer.stubFor(post(urlPathEqualTo("/news"))
				.willReturn(
				aResponse().withHeader("Content-Type", "application/json").withBody("")));
	}

	private void mockNewsFeed(WireMockServer wireMockServer) throws IOException, FeedException {
		SyndFeedOutput output = new SyndFeedOutput();
		StringWriter writer = new StringWriter();
		output.output(generateRssFeed(), writer);
		wireMockServer.stubFor(get(urlPathEqualTo("/"))
				.willReturn(
				aResponse().withHeader("Content-Type", "application/rss+xml").withBody(writer.toString())));
	}

	private WireMockServer startWireMock() {
		WireMockServer wireMockServer = new WireMockServer(options()
				.port(Integer.parseInt(WIREMOCK_PORT))
				.notifier(new ConsoleNotifier(true)));
		wireMockServer.start();
		return wireMockServer;
	}
	
	private SyndFeed generateRssFeed() {
		SyndFeed feed = new SyndFeedImpl();
		for (int i = 0; i < 10; i++) {
			feed.getEntries().add(generateRssEntry(i));
		}
		feed.setFeedType("rss_2.0");
		feed.setTitle("Test Feed");
		feed.setDescription("This is a test feed");
		feed.setLink("http://localhost:" + WIREMOCK_PORT);
		return feed;
	}
	
	private SyndEntry generateRssEntry(int item) {
		SyndEntry entry = new SyndEntryImpl();
		entry.setAuthor(String.format("%s %s", faker.name().firstName(), faker.name().lastName()));
		entry.setTitle(faker.lorem().fixedString(64));
		SyndContent desc = new SyndContentImpl();
		desc.setValue(String.format("%s %s %s", faker.lorem().fixedString(20), faker.country().capital(), faker.lorem().fixedString(20)));
		entry.setDescription(desc);
		entry.setLink("http://localhost:" + WIREMOCK_PORT + "/item/" + item);
		return entry;
	}
	
	private PropertiesSource testConfiguration() {
		return new PropertiesSource() {
			
			@Override
			public String getProperty(String name) {
				switch (name) {
				case "scraper.feeds.url":
					return FEEDS_URL;	
				case "scraper.feed.backend.host":
					return "localhost";
				case "scraper.feed.backend.port":
					return WIREMOCK_PORT;	
				case "scraper.feeds.delay":
					return DELAY_MS;
				default:
					return null;
				}
				
			}
			
			@Override
			public String getName() {
				return "testPropertySource";
			}
		};
	}
	
	@Override
    protected RouteBuilder createRouteBuilder() {
		RssReader reader = new RssReader();
		reader.feedsUrl = FEEDS_URL;
		reader.host = "localhost";
		reader.port = WIREMOCK_PORT;
		reader.delay = Integer.parseInt(DELAY_MS);
        return reader;
    }
}
