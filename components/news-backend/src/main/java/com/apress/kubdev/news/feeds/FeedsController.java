package com.apress.kubdev.news.feeds;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apress.kubdev.news.core.NewsService;
import com.apress.kubdev.news.core.domain.model.News;


@Path("/news")
public class FeedsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FeedsController.class);

	@Inject
	FeedItemMapper mapper;
	@Inject
	NewsService newsService;
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Counted(absolute = true, name = "createFeedsRequest", description = "How many feed have been requested to be created.")
	public Response createNewsFromFeed(FeedItem item, @Context UriInfo uriInfo) {
		News createdNews = newsService.createNews(mapper.toNews(item));
		LOGGER.info("Created new Feed Item {}", createdNews);
		Long newsId = createdNews.getId();
		UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(Long.toString(newsId));
		return Response.created(uriBuilder.build()).build();
	}
	
	@Gauge(absolute = true, name = "duplicateFeeds", unit = MetricUnits.NONE, description = "How many feeds have already been stored and are skipped as duplicates.")
    public Long feedsAnalyzed() {
        return newsService.getFeedDuplicates();
    }
}
