package com.apress.kubdev.rss.crd;

import java.util.ArrayList;
import java.util.List;

public class FeedAnalysisSpec {
	private List<String> urls = new ArrayList<>();

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}
}
