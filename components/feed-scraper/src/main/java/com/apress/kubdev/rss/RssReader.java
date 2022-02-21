package com.apress.kubdev.rss;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RssReader extends RouteBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RssReader.class);
	
	@PropertyInject(value = "?scraper.feeds.lastUpdate")
    String lastUpdateString;
	
	@PropertyInject(value = "scraper.feed.backend.host", defaultValue = "localhost")
    String host;
	
	@PropertyInject(value = "scraper.feed.backend.port", defaultValue = "8080")
    String port;
	
	@PropertyInject(value = "scraper.feeds.repeat", defaultValue = "0")
    int repeat;
	
	@PropertyInject(value = "scraper.feeds.delay", defaultValue = "100")
    int delay;
	
	@PropertyInject(value = "scraper.feeds.url")
    String feedsUrl;
	
	
	@Override
	public void configure() throws Exception {
		try(JaxbDataFormat xmlDataFormat = new JaxbDataFormat()){
			JAXBContext con = JAXBContext.newInstance(RssItem.class);
			xmlDataFormat.setContext(con);
			LOGGER.info("Target backend is {}:{}", host, port);
			LOGGER.info("Repeat {} times every {} ms", repeat, delay);
			restConfiguration().producerComponent("http").bindingMode(RestBindingMode.json);//.host(host).port(port).bindingMode(RestBindingMode.json);
			Optional<String> lastUpdate = Optional.ofNullable(lastUpdateString);
			lastUpdate.map(LocalDateTime::parse).map(Timestamp::valueOf).ifPresent(t -> bindToRegistry("lastUpdate", t));
			String lastUpdateParam = lastUpdate.map(u -> "&lastUpdate=#lastUpdate").orElse("");
			lastUpdate.ifPresent(u -> LOGGER.info("Last update set to {}", u));
			String parameters = "?splitEntries=true&repeatCount={{scraper.feeds.repeat:0}}&delay={{scraper.feeds.delay:100}}&sortEntries=true" + lastUpdateParam;
			
			String[] feedsUrlElements = feedsUrl.split(",");
			LOGGER.info("Analyzing {} feed urls", feedsUrlElements.length);
			
			onException(IOException.class)
				.maximumRedeliveries("{{scraper.feed.backend.maximumRedeliveries:0}}")
				.useExponentialBackOff()
				.redeliveryDelay("{{scraper.feed.backend.redeliveryDelay:10000}}")
				.onRedelivery(p -> LOGGER.info("Redelivered due to IOException"));
			
			onException(HttpOperationFailedException.class)
				.onWhen(simple("${exception.statusCode} == 409"))
					.log("Duplicate news")
					.handled(true)
				.onWhen(simple("${exception.statusCode} == 500"))
					.maximumRedeliveries("{{scraper.feed.backend.maximumRedeliveries:0}}")
					.useExponentialBackOff()
					.redeliveryDelay("{{scraper.feed.backend.redeliveryDelay:10000}}")
					.onRedelivery(p -> LOGGER.info("Redelivered due to Internal Server error"));
			
			for (int i = 0; i < feedsUrlElements.length; i++) {
				LOGGER.info("Starting with {} feed ", feedsUrlElements[i]);
				from("rss:" + feedsUrlElements[i].trim() + parameters)
					.marshal().rss()
					.split().xpath("//item")
					.log(LoggingLevel.DEBUG, "${body}")
					.unmarshal(xmlDataFormat)
					.process(rssItemToFeedItem())
					.marshal().json()
					.log("${body}")
					.to("rest:post:news?host={{scraper.feed.backend.host:localhost}}:{{scraper.feed.backend.port:8080}}");
			}
			
				
		}catch(Exception e){
			LOGGER.error("Error configuring route", e);
		}
	}
	
	void sendNoMoreRepeatsEvent(Exchange e) {
		LOGGER.info("NO MORE REPEATS");
	}
	

	private Processor rssItemToFeedItem() {
		return ex -> {
			RssItem rssItem = (RssItem) ex.getIn().getBody();
			FeedItem item = new FeedItem(rssItem.getTitle(), rssItem.getLink(), rssItem.getDescription());
			ex.getIn().setBody(item);
		};
	}

}
