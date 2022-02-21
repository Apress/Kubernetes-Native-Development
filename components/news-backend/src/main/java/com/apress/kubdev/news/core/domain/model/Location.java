package com.apress.kubdev.news.core.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Location {
	private final BigDecimal longitude;
	private final BigDecimal latitude;

	private Location(Builder builder) {
		this.longitude = builder.longitude;
		this.latitude = builder.latitude;
	}
	
	public BigDecimal getLongitude() {
		return longitude;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude);
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
		Location other = (Location) obj;
		return Objects.equals(latitude, other.latitude) && Objects.equals(longitude, other.longitude);
	}

	@Override
	public String toString() {
		return String.format("Location [longitude=%s, latitude=%s]", longitude, latitude);
	}

	public static Builder builder() {
		return new Builder();
	}


	public static final class Builder {
		private BigDecimal longitude;
		private BigDecimal latitude;

		private Builder() {
		}

		public Builder withLongitude(BigDecimal longitude) {
			this.longitude = longitude;
			return this;
		}

		public Builder withLatitude(BigDecimal latitude) {
			this.latitude = latitude;
			return this;
		}
		
		public Builder withLongitude(double longitude) {
			this.longitude = BigDecimal.valueOf(longitude);
			return this;
		}

		public Builder withLatitude(double latitude) {
			this.latitude = BigDecimal.valueOf(latitude);
			return this;
		}

		public Location build() {
			return new Location(this);
		}
	}
	
}
