package com.apress.kubdev.rss;

import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestEndpoint extends RouteBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RestEndpoint.class);
	
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
	
	@Override
	public void configure() throws Exception {
		LOGGER.debug("Starting Rest Endpoint to receive additional analyzes after startup");
		restConfiguration().host("0.0.0.0").port(9090).bindingMode(RestBindingMode.json);
		from("rest:post:analysis")
			.log("Analyzing ${body}")
			.process(p-> {
				RssReader reader = new RssReader();
				String body = p.getIn().getBody(String.class);
				reader.feedsUrl = body.substring(1).substring(0, body.length() - 2).replaceAll("\\\"", "");
				reader.lastUpdateString = this.lastUpdateString;
				reader.host = this.host;
				reader.port = this.port;
				reader.repeat = this.repeat;
				reader.delay = this.delay;
				getContext().addRoutes(reader);
			});
	}

}
