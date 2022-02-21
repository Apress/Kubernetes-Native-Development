package com.apress.kubdev.rss;

public class FeedAnalysis {
	private String apiVersion;
	private String kind;
	private FeedAnalysisMetadata metadata;
	private FeedAnalysisSpec spec;
	
	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public FeedAnalysisMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(FeedAnalysisMetadata metatdata) {
		this.metadata = metatdata;
	}
	
	public FeedAnalysisSpec getSpec() {
		return spec;
	}

	public void setSpec(FeedAnalysisSpec spec) {
		this.spec = spec;
	}

	@Override
	public String toString() {
		return String.format("FeedAnalysis [apiVersion=%s]", apiVersion);
	}
	
	
}
