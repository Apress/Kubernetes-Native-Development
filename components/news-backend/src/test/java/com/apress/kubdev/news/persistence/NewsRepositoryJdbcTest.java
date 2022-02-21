package com.apress.kubdev.news.persistence;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class NewsRepositoryJdbcTest {
	
	@Inject
	NewsRepositoryJdbc repository;
	
	
	@Test
	public void startUp() {
		repository.startup(null);
	}
}
