package fr.supmap.supmapapi.utils;

import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class GeoUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public static String encodeLineStringToPolyline(LineString line) {
        return encodeCoordinates(line.getCoordinates(), 1e5);
    }

    private static String encodeCoordinates(Coordinate[] coords, double precision) {
        StringBuilder result = new StringBuilder();
        int prevLat = 0, prevLng = 0;

        for (Coordinate coord : coords) {
            int lat = (int) Math.round(coord.getY() * precision);
            int lng = (int) Math.round(coord.getX() * precision);

            int dlat = lat - prevLat;
            int dlng = lng - prevLng;

            encodeSignedNumber(dlat, result);
            encodeSignedNumber(dlng, result);

            prevLat = lat;
            prevLng = lng;
        }
        return result.toString();
    }

    private static void encodeSignedNumber(int num, StringBuilder sb) {
        int sgnNum = num << 1;
        if (num < 0) {
            sgnNum = ~sgnNum;
        }
        encodeUnsignedNumber(sgnNum, sb);
    }

    private static void encodeUnsignedNumber(int num, StringBuilder sb) {
        while (num >= 0x20) {
            int nextValue = (0x20 | (num & 0x1f)) + 63;
            sb.append((char) (nextValue));
            num >>= 5;
        }
        sb.append((char) (num + 63));
    }

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

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lat += dlat;

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
            coordinates.add(new Coordinate(longitude, latitude));
        }

        return coordinates;
    }
}
