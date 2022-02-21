package com.apress.kubdev.news.feeds;

import java.net.MalformedURLException;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;

import com.apress.kubdev.news.core.domain.model.News;

@ApplicationScoped
public class FeedItemMapper {
	
	public News toNews(FeedItem item) {
		try {
			return News.builder()
				.withTitle(item.getTitle())
				.withDescription(item.getDescription())
				.withLink(new URL(item.getLink()))
				.build();
		} catch (MalformedURLException e) {
			throw new BadRequestException("Error converting " + item, e);
		}
	}
}
