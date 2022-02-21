package com.apress.kubdev.rss;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.io.FeedException;

import jakarta.xml.bind.JAXBException;

public class Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	private static final String NEWS_RESOURCE_PATH = "/news";
	
	public static void main(String[] args) throws IllegalArgumentException, FeedException, IOException, JAXBException {
		LOGGER.debug("Starting scraper");
		RssReader reader = new RssReader();
		
		URL feedsUrl = new URL(fromEnvOrProperty("scraper.feeds.url"));
		LOGGER.debug("Feeds url is {}", feedsUrl);
		URL backendUrl = new URL("http", fromEnvOrProperty("scraper.feed.backend.host"),
				Integer.parseInt(fromEnvOrProperty("scraper.feed.backend.port")), NEWS_RESOURCE_PATH);
		LOGGER.debug("Backend url is {}", backendUrl);
		reader.readRss(feedsUrl, backendUrl);
	}
	
	private static String fromUpperCaseEnv(String key) {
		return System.getenv((key.toUpperCase().replace(".", "_")));
	}

	private static String fromEnvOrProperty(String key) {
		String urlEnv = fromEnvOrSystem(key);
		if (urlEnv != null) {
			return urlEnv;
		} else {
			urlEnv = fromUpperCaseEnv(key);
			if (urlEnv != null) {
				return urlEnv;
			} else {
				return ApplicationProperties.getString(key);				
			}
		}
	}
	
	private static String fromEnvOrSystem(String key) {
		String urlEnv = System.getenv(key);
		if (urlEnv == null) {
			return System.getProperty(key);
		} else {
			return urlEnv;
		}
	}

}
