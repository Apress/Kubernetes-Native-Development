package com.apress.kubdev.news.core;

public class NewsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final NewsFault fault;

	public NewsException() {
		super();
		fault = NewsFault.UNKNOWN;
	}

	public NewsException(String message, Throwable cause) {
		super(message, cause);
		fault = NewsFault.UNKNOWN;
	}

	public NewsException(String message, NewsFault fault) {
		super(message);
		this.fault = fault;
	}

	public NewsException(String message) {
		super(message);
		fault = NewsFault.UNKNOWN;
	}

	public NewsException(Throwable cause) {
		super(cause);
		fault = NewsFault.UNKNOWN;
	}

	public NewsFault getFault() {
		return fault;
	}

}
