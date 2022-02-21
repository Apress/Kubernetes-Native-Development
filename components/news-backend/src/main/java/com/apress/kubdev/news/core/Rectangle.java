package com.apress.kubdev.news.core;

import java.util.Objects;

import com.apress.kubdev.news.core.domain.model.Location;

public class Rectangle {
	
	private final Location southWest; 
	private final Location northEast;
	
	private Rectangle(Builder builder) {
		this.southWest = builder.southWest;
		this.northEast = builder.northEast;
	}
	
	public Location getSouthWest() {
		return southWest;
	}

	public Location getNorthEast() {
		return northEast;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Location southWest;
		private Location northEast;

		private Builder() {
		}

		public Builder withSouthWest(Location southWest) {
			this.southWest = southWest;
			return this;
		}

		public Builder withNorthEast(Location northEast) {
			this.northEast = northEast;
			return this;
		}

		public Rectangle build() {
			return new Rectangle(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(northEast, southWest);
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
		Rectangle other = (Rectangle) obj;
		return Objects.equals(northEast, other.northEast) && Objects.equals(southWest, other.southWest);
	}

	@Override
	public String toString() {
		return String.format("Rectangle [southWest=%s, northEast=%s]", southWest, northEast);
	}
	
}

