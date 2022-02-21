package com.apress.kubdev.news.persistence;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.postgis.Geometry;
import org.postgis.LinearRing;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import com.apress.kubdev.news.core.Rectangle;
import com.apress.kubdev.news.core.domain.model.Location;

@ApplicationScoped
public class GeometryService {
	
	public PGgeometry fromRectangle(Rectangle rectangle) {
		Point[] points = new Point[] {
				new Point(getX(rectangle.getSouthWest()), getY(rectangle.getSouthWest())),
				new Point(getX(rectangle.getNorthEast()), getY(rectangle.getSouthWest())),
				new Point(getX(rectangle.getNorthEast()), getY(rectangle.getNorthEast())),
				new Point(getX(rectangle.getSouthWest()), getY(rectangle.getNorthEast())),
				new Point(getX(rectangle.getSouthWest()), getY(rectangle.getSouthWest()))
		};
		Polygon polygon = new Polygon(new LinearRing[]{new LinearRing(points)});
		return new PGgeometry(polygon);
	}
	
	public Location toLocation(Point point) {
		return Location.builder().withLatitude(point.x).withLongitude(point.y).build();
	}
	
	public Optional<Location> toLocation(PGgeometry geometry) {
		if(geometry != null && geometry.getGeoType() == Geometry.POINT) {
			return Optional.ofNullable(toLocation((Point) geometry.getGeometry()));
		} else if (geometry != null) {
			return Optional.empty();
		} else {
			return Optional.empty();
		}
	}
	
	public PGgeometry fromLocation(Location location) {
		return new PGgeometry(new Point(getX(location), getY(location)));
	}
	
	private double getX(Location location) {
		return location.getLatitude().doubleValue();
	}
	
	private double getY(Location location) {
		return location.getLongitude().doubleValue();
	}
}
