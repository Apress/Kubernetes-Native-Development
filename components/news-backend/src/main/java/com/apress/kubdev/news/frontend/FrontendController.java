package com.apress.kubdev.news.frontend;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apress.kubdev.news.core.NewsService;
import com.apress.kubdev.news.core.Rectangle;
import com.apress.kubdev.news.core.domain.model.Location;
import com.apress.kubdev.news.core.domain.model.News;

@Path("/news")
public class FrontendController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FrontendController.class);
	
	@Inject
	NewsService newsService;
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/{id}")
	public Response getNewsById(@PathParam("id") Long id) {
		return newsService.findNewsById(id).map(n -> Response.ok(n).build())
				.orElseGet(() -> Response.status(Status.NOT_FOUND).build());
	}

	
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getNewsByBounds(
			@QueryParam("sw.lat") Optional<BigDecimal> swLat,
			@QueryParam("sw.lng") Optional<BigDecimal> swLng,
			@QueryParam("ne.lat") Optional<BigDecimal> neLat,
			@QueryParam("ne.lng") Optional<BigDecimal> neLng) {
		
		if (swLat.isPresent() && swLng.isPresent() && neLat.isPresent() && neLng.isPresent()) {
			LOGGER.debug("Query news");
			Location southWest = Location.builder().withLatitude(swLat.get()).withLongitude(swLng.get()).build();
			Location northEast = Location.builder().withLatitude(neLat.get()).withLongitude(neLng.get()).build();
			Rectangle bounds = Rectangle.builder().withSouthWest(southWest).withNorthEast(northEast).build();
			List<News> news = newsService.findNewsByBounds(bounds);
			return Response.ok(news).build();
		} else {
			LOGGER.info("No bounds");
			return Response.ok(Collections.emptyList()).build();
		}
	}
	
	@Gauge(absolute = true, name = "feedsTotal", unit = MetricUnits.NONE, description = "Feeds in database.")
    public Long feedsAnalyzed() {
        return newsService.getFeedDuplicates();
    }
	
	
}
