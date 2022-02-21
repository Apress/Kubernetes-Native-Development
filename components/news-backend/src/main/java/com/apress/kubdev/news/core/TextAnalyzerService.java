package com.apress.kubdev.news.core;

import java.util.Optional;

import com.apress.kubdev.news.core.domain.model.Location;
import com.apress.kubdev.news.core.domain.model.News;

public interface TextAnalyzerService {
	Optional<Location> getLocation(News news);
}
