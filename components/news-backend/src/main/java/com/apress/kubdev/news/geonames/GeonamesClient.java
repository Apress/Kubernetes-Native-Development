package com.apress.kubdev.news.geonames;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

//http://api.geonames.org/searchJSON?q=london&maxRows=10&username=demo
@RegisterRestClient
public interface GeonamesClient {

	@GET
	@Path("/searchJSON")
	@Produces({MediaType.APPLICATION_JSON})
	public GeonameResult search(@QueryParam("q") String query, @QueryParam("maxRows") Integer maxRows,
			@QueryParam("username") String username);
}
