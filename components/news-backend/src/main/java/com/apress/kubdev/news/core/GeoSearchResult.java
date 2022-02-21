package com.apress.kubdev.news.core;

import java.util.Objects;

import com.apress.kubdev.news.core.domain.model.Location;

public class GeoSearchResult {
	private final String locationName;
	private final Location location;
	
	public GeoSearchResult(String locationName, Location location) {
		super();
		this.locationName = locationName;
		this.location = location;
	}
	public String getLocationName() {
		return locationName;
	}
	public Location getLocation() {
		return location;
	}
	@Override
	public int hashCode() {
		return Objects.hash(location, locationName);
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
		GeoSearchResult other = (GeoSearchResult) obj;
		return Objects.equals(location, other.location) && Objects.equals(locationName, other.locationName);
	}
	@Override
	public String toString() {
		return String.format("GeoSearchResult [locationName=%s, location=%s]", locationName, location);
	}
}
