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

    public static Point parsePoint(String coordinates) {
        String[] parts = coordinates.split(",");
        double lat = Double.parseDouble(parts[0].trim());
        double lon = Double.parseDouble(parts[1].trim());
        // JTS attend (x, y) = (lon, lat)
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }

    public static LineString decodePolylineToLineString(String encodedPolyline) {
        List<Coordinate> coords = decodePolyline(encodedPolyline);
        return geometryFactory.createLineString(coords.toArray(new Coordinate[0]));
    }

    private static List<Coordinate> decodePolyline(String encoded) {
        // Implémentez le décodage de polyline selon l'algorithme de Google ou utilisez une librairie existante.
        return new ArrayList<>();
    }
}
