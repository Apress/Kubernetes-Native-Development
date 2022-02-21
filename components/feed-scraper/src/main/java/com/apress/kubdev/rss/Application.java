package com.apress.kubdev.rss;

import org.apache.camel.main.Main;

public class Application {
	
	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.configure().addRoutesBuilder(RssReader.class);
		main.configure().addRoutesBuilder(RestEndpoint.class);
		main.configure().addRoutesBuilder(CrdRoute.class);
		//main.configure().setTracing(true);
		main.run(args);
	}
}
