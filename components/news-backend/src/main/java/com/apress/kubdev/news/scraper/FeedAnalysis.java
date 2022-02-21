package com.apress.kubdev.news.scraper;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;

@Version(FeedAnalysis.VERSION)
@Group(FeedAnalysis.GROUP)
@Plural(FeedAnalysis.PLURAL)
public class FeedAnalysis extends CustomResource<FeedAnalysisSpec, Void> implements Namespaced {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FeedAnalysisSpec spec;
	
	public static final String GROUP = "kubdev.apress.com";
	public static final String VERSION = "v1";
	public static final String PLURAL = "feedanalyses";

	@Override
	public FeedAnalysisSpec getSpec() {
		return spec;
	}
	
	@Override
	public void setSpec(FeedAnalysisSpec spec) {
		this.spec = spec;
	}
}
