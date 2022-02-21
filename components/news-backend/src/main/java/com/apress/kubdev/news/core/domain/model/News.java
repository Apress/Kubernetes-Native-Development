package com.apress.kubdev.news.core.domain.model;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Generated;

public class News {
	
	private final Long id;
	private final String title;
	private final URL link;
	private final String description;
	private final Optional<Location> location;

	@Generated("SparkTools")
	private News(Builder builder) {
		this.id = builder.id;
		this.title = builder.title;
		this.link = builder.link;
		this.description = builder.description;
		this.location = builder.location;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public URL getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}
	
	public Optional<Location> getLocation() {
		return location;
	}
	
	public News pintoLocation(Location location) {
		return News.builder()
			.withId(this.getId())
			.withTitle(this.getTitle())
			.withLink(this.getLink())
			.withDescription(this.getDescription())
			.withLocation(Optional.of(location))
			.build();
	}
	
	public News persisted(Long id) {
		return News.builder()
			.withId(id)
			.withTitle(this.getTitle())
			.withLink(this.getLink())
			.withDescription(this.getDescription())
			.withLocation(this.location)
			.build();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(link);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		News other = (News) obj;
		return Objects.equals(link, other.link);
	}

	@Override
	public String toString() {
		return String.format("News [id=%s, title=%s, link=%s, description=%s, location=%s]", id, title, link,
				description, location);
	}

	/**
	 * Creates builder to build {@link News}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link News}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private Long id;
		private String title;
		private URL link;
		private String description;
		private Optional<Location> location = Optional.empty();

		private Builder() {
		}

		public Builder withId(Long id) {
			this.id = id;
			return this;
		}

		public Builder withTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder withLink(URL link) {
			this.link = link;
			return this;
		}

		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder withLocation(Optional<Location> location) {
			this.location = location;
			return this;
		}

		public News build() {
			return new News(this);
		}
	}
	
}
