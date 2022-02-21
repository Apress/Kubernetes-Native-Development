package com.apress.kubdev.news.extractor;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
public interface LocationExtractorClient {
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Counted(absolute = true, name = "feedsAnalyzeRequests", description = "How many feed analyzer request have been sent.")
	Map<String, AnalysisResult> analyzeText(@QueryParam("text") String text);
}
