package com.apress.kubdev.news.config;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;

@ApplicationScoped
public class Configuration {
	private WireMockServer wireMockServer;
	
	void startup(@Observes StartupEvent event) { 
		String profile = ProfileManager.getActiveProfile();
		if ("dev".equalsIgnoreCase(profile) || ("test".equalsIgnoreCase(profile))) {
			wireMockServer = new WireMockServer(options()
					.port(9081)
					.notifier(new ConsoleNotifier(true)));
			wireMockServer.start(); 
			wireMockServer.stubFor(get(urlPathEqualTo("/searchJSON"))
					.withQueryParam("q", equalTo("Frankfurt"))
					.withQueryParam("maxRows", equalTo("5"))
					.withQueryParam("username", equalTo("demo"))
					.willReturn(
					aResponse().withHeader("Content-Type", "application/json").withBody("")));
			wireMockServer.stubFor(get(urlPathEqualTo("/"))
					.willReturn(
							okJson("{\n"
									+ "    \"1\": {\n"
									+ "        \"extracted location\": \"Frankfurt\",\n"
									+ "        \"generated address\": \"Frankfurt am Main, Hessen, Deutschland\",\n"
									+ "        \"latitude\": 50.1106444,\n"
									+ "        \"longitude\": 8.6820917\n"
									+ "    }\n"
									+ "}")
			));
		}
	}
	
	void teardown(@Observes ShutdownEvent event) { 
		if (wireMockServer != null) {
			wireMockServer.stop();
		}
	}
}
