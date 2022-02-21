package com.apress.kubdev.news.core;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import com.apress.kubdev.news.core.domain.model.News;

public interface NewsRepository {
	
	public News create(News changed);
	public News update(News changed);
	public Optional<News> findById(Long id);
	public void delete(News changed);
	public List<News> findByGeometry(Rectangle bounds);
	public Optional<News> findByLink(URL link);
	long count();
	void deleteAll();
}
