package com.apress.kubdev.news.feeds;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.apress.kubdev.news.core.NewsException;
import com.apress.kubdev.news.core.NewsFault;

@Provider
public class NewsExceptionHandler implements ExceptionMapper<NewsException>{
	
	@Override
	public Response toResponse(NewsException exception) {
		if (exception.getFault() == NewsFault.DUPLICATE) {
			return Response.status(Status.CONFLICT).entity(exception.getMessage()).build();  
		} else {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();  
		}
	}
	
	

}
