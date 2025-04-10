package fr.supmap.supmapapi.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.List;

public class GeoUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public static Point createPoint(Double latitude, Double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    public static Point parsePoint(String coordinates) {
        String[] parts = coordinates.split(",");
        double lat = Double.parseDouble(parts[0].trim());
        double lon = Double.parseDouble(parts[1].trim());
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }

    public static LineString decodePolylineToLineString(String encodedPolyline) {
        List<Coordinate> coords = decodePolyline(encodedPolyline, 1e5);
        return geometryFactory.createLineString(coords.toArray(new Coordinate[0]));
    }

    private static List<Coordinate> decodePolyline(String encoded, double precision) {
        List<Coordinate> coordinates = new ArrayList<>();
        int index = 0, lat = 0, lng = 0;

        while (index < encoded.length()) {
            int b, shift = 0, result = 0;

            // Décodage latitude
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            // Décodage longitude
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            double latitude = lat / precision;
            double longitude = lng / precision;
            coordinates.add(new Coordinate(longitude, latitude)); // Attention: lon, lat (X,Y)
        }

        return coordinates;
    }
}
