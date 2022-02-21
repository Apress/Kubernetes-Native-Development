package com.apress.kubdev.news.extractor;

import java.math.BigDecimal;

import javax.json.bind.annotation.JsonbProperty;

public class AnalysisResult {
	
	@JsonbProperty("extracted location")
	private String locationName;
	
	@JsonbProperty("generated address")
	private String address;
	
	@JsonbProperty("longitude")
	private BigDecimal longitude;
	
	@JsonbProperty("latitude")
	private BigDecimal latitude;

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}
	
}
