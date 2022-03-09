package com.apress.kubdev.rss;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class RssReader {
	private static final Logger LOGGER = LoggerFactory.getLogger(RssReader.class);
	
	private final OkHttpClient httpClient = new OkHttpClient();
	
	
	public void readRss(URL feedUrl, URL backendUrl) throws IllegalArgumentException, FeedException, IOException, JAXBException {
		Marshaller marshaller = JAXBContext.newInstance(FeedItem.class).createMarshaller();
		marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, org.eclipse.persistence.oxm.MediaType.APPLICATION_JSON);
		marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
		SyndFeedInput input = new SyndFeedInput();
	    SyndFeed feed = input.build(new XmlReader(feedUrl));
	    feed.getEntries().stream().map(e -> new FeedItem(e.getTitle(), e.getLink(), e.getDescription() != null ? e.getDescription().getValue() : ""))
	    	.map(f -> feedToJson(marshaller, f))
	    	.map(txt -> RequestBody.create(
	            MediaType.parse("application/json; charset=utf-8"),
	            txt))
	    	.map(body -> new Request.Builder()
	                .url(backendUrl)
	                .addHeader("User-Agent", "OkHttp Bot")
	                .post(body)
	                .build())
	    	.map(req -> {
				try {
					return httpClient.newCall(req).execute();
				} catch (IOException e) {
					LOGGER.error("Error calling backend service", e);
					return null;
				}
			}) 
	    	.map(res -> 
	    		res != null ? 
	    			String.format("%s : %s", res.code(), res.message()) : "no response"
			)
	    	.forEach(res -> {
	    		if (res != null) {
	    			LOGGER.info(res);
	    		}
	    	});
	}
	


	private String feedToJson(Marshaller marshaller, FeedItem item) {
		StringWriter sw = new StringWriter();
		try {
			marshaller.marshal(item, sw);
		} catch (JAXBException e) {
			LOGGER.error("Error marshalling item to xml", e);
		}
		String result = sw.toString();
		LOGGER.info(result);
		return result;
	}
}
