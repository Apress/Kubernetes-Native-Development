package com.apress.kubdev.rss;

import org.apache.camel.Exchange;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apress.kubdev.rss.crd.FeedAnalysis;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;

public class CrdRoute extends RouteBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CrdRoute.class);
	private ObjectMapper mapper = new ObjectMapper();
	@PropertyInject(value = "scraper.crd.enable")
    boolean crdEnabled;

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
		if (crdEnabled) {
			bindToRegistry("kubernetesClient", new DefaultKubernetesClient());
			LOGGER.debug("Watching CRD");
			from("kubernetes-custom-resources:///?kubernetesClient=#kubernetesClient&operation=createCustomResource&crdName=feedanalysis&crdGroup=kubdev.apress.com&crdScope=namespaced&crdVersion=v1&crdPlural=feedanalyses")
				.log("Watched creation of ${body}")
				.process(p -> {
					FeedAnalysis feedAnalysis = mapper.readValue(p.getIn().getBody().toString(), FeedAnalysis.class);
					p.getIn().setBody(mapper.writeValueAsString(feedAnalysis.getSpec().getUrls()));
				})
				.process(this::startRssReader);
		}
	}
	private void startRssReader(Exchange p) throws Exception {
		RssReader reader = new RssReader();
		String body = p.getIn().getBody(String.class);
		reader.feedsUrl = body.substring(1).substring(0, body.length() - 2).replaceAll("\\\"", "");
		reader.lastUpdateString = this.lastUpdateString;
		reader.host = this.host;
		reader.port = this.port;
		reader.repeat = this.repeat;
		reader.delay = this.delay;
		getContext().addRoutes(reader);
	}

}
