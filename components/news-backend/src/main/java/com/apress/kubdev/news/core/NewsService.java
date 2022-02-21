package com.apress.kubdev.news.core;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.ProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apress.kubdev.news.core.domain.model.Location;
import com.apress.kubdev.news.core.domain.model.News;

@ApplicationScoped
public class NewsService {
	private static final Logger LOGGER = LoggerFactory.getLogger(NewsService.class);
	@Inject
	NewsRepository newsRepository;
	@Inject
	TextAnalyzerService newsTextAnalyzerService;
	private long feedDuplicates;
	
	@Transactional
	public News createNews(News news) {
		assertNewsNotExists(news);
		News created = newsRepository.create(news);
		LOGGER.info("Created new Feed Item {}", created);
		Optional<Location> analysisResult = analyzeText(created);
		News pinnedtoLocation = analysisResult.map(r -> created.pintoLocation(r)).orElse(created);
		return newsRepository.update(pinnedtoLocation);
	}

	private void assertNewsNotExists(News news) {
		if (newsRepository.findByLink(news.getLink()).isPresent()) {
			feedDuplicates++;
			throw new NewsException(String.format("News with same link '%s' already exists", news.getLink()),
					NewsFault.DUPLICATE);
		}
	}

	private Optional<Location> analyzeText(News created) {
		try {
			return newsTextAnalyzerService.getLocation(created);
		} catch (ProcessingException e) {
			LOGGER.error("Error connecting to text analyzer service. Cannot pin news to location.", e);
			throw new NewsException(String.format("Error connecting to text analyzer service. Cannot pin news '%s' to location.", created.getLink()),
					NewsFault.ANALYSIS_ERROR);
		}
	}
	
	public List<News> findNewsByBounds(Rectangle bounds){	
		return newsRepository.findByGeometry(bounds);
	}
	
	public Optional<News> findNewsById(Long id){	
		return newsRepository.findById(id);
	}

	public long getFeedDuplicates() {
		return feedDuplicates;
	}
	
	public long getFeedCount() {
		return newsRepository.count();
	}

}
