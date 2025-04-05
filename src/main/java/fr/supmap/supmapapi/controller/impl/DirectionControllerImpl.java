package fr.supmap.supmapapi.controller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.supmap.supmapapi.controller.DirectionController;
import fr.supmap.supmapapi.model.dto.DirectionsDto;
import fr.supmap.supmapapi.model.entity.table.Route;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.RouteRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.utils.GeoUtils;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class DirectionControllerImpl implements DirectionController {

    @Value("${graphhopper.api-key}")
    private String graphhopperApiKey;

    @Value("${graphhopper.base-url}")
    private String graphhopperBaseUrl;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * @param origin      Peut être au format "lat,lon" ou une adresse (ex: "Paris")
     * @param mode        "car", "bike", "foot", etc.
     * @param destination Identique à origin (coordonnées ou texte)
     * @return La réponse JSON brute de l'API GraphHopper
     */
    @Override
    public String getDirection(String origin, String mode, String destination) {
        String originCoordinates = getCoordinates(origin);
        String destinationCoordinates = getCoordinates(destination);

        String url = graphhopperBaseUrl + "/route?"
                + "point=" + originCoordinates
                + "&point=" + destinationCoordinates
                + "&profile=" + mode;

        log.info(url);

        RestTemplate restTemplate = new RestTemplate();
        log.info("GET " + url);
        String ghResponse = restTemplate.getForObject(url, String.class);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer userId = Integer.parseInt(authentication.getName());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(ghResponse);
            JsonNode pathsNode = root.path("paths");
            if (pathsNode.isArray() && pathsNode.size() > 0) {
                JsonNode firstPath = pathsNode.get(0);
                double distance = firstPath.path("distance").asDouble();
                double timeMs = firstPath.path("time").asDouble();
                String encodedPolyline = firstPath.path("points").asText();

                Point startPoint = GeoUtils.parsePoint(originCoordinates);
                Point endPoint = GeoUtils.parsePoint(destinationCoordinates);
                LineString routeGeometry = GeoUtils.decodePolylineToLineString(encodedPolyline);

                Route route = new Route();
                route.setUser(user);
                route.setTotalDistance(distance);
                route.setTotalDuration(timeMs / 1000.0);
                route.setStartLocation(startPoint);
                route.setEndLocation(endPoint);
                route.setRouteGeometry(routeGeometry);
                route.setCalculatedAt(Instant.now());

                routeRepository.save(route);
                log.info("Route sauvegardée pour l'utilisateur " + user.getUsername());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde de la route", e);
        }

        return ghResponse;
    }

    /**
     * @param origin      Peut être au format "lat,lon" ou une adresse (ex: "Paris")
     * @param mode        "car", "bike", "foot", etc.
     * @param destination Identique à origin (coordonnées ou texte)
     * @return Les 3 itinéraires alternatifs (le plus rapide, le plus économique, sans péage)
     */
    @Override
    public DirectionsDto getDirections(String origin, String mode, String destination) {
        String originCoordinates = getCoordinates(origin);
        String destinationCoordinates = getCoordinates(destination);

        RestTemplate restTemplate = new RestTemplate();
        DirectionsDto dto = new DirectionsDto();

        try {
            ObjectMapper mapper = new ObjectMapper();

            // POINTS
            List<List<Double>> points = List.of(
                    List.of(Double.parseDouble(originCoordinates.split(",")[1]), Double.parseDouble(originCoordinates.split(",")[0])),
                    List.of(Double.parseDouble(destinationCoordinates.split(",")[1]), Double.parseDouble(destinationCoordinates.split(",")[0]))
            );

            // HEADERS
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Requête 1 : normal + alternatif
            Map<String, Object> bodyFastest = new java.util.HashMap<>();
            bodyFastest.put("points", points);
            bodyFastest.put("profile", mode);
            bodyFastest.put("elevation", true);
            bodyFastest.put("instructions", true);
            bodyFastest.put("locale", "fr_FR");
            bodyFastest.put("points_encoded", true);
            bodyFastest.put("points_encoded_multiplier", 100_000);
            bodyFastest.put("details", List.of("road_class", "road_environment", "max_speed", "average_speed"));
            bodyFastest.put("snap_preventions", List.of("ferry"));
            bodyFastest.put("ch.disable", true);

            HttpEntity<String> requestFastest = new HttpEntity<>(mapper.writeValueAsString(bodyFastest), headers);
            String responseFastest = restTemplate.postForObject(graphhopperBaseUrl + "/route?key=", requestFastest, String.class);
            dto.setFastest(responseFastest);

            // Requête 2 : sans autoroute
            Map<String, Object> priorityRule = new java.util.HashMap<>();
            priorityRule.put("if", "road_class == MOTORWAY");
            priorityRule.put("multiply_by", 0.0);

            Map<String, Object> customModel = new java.util.HashMap<>();
            customModel.put("priority", List.of(priorityRule));

// Maintenant ajoute-le dans le bodyNoHighway
            Map<String, Object> bodyNoHighway = new java.util.HashMap<>();
            bodyNoHighway.put("points", points);
            bodyNoHighway.put("profile", mode);
            bodyNoHighway.put("elevation", true);
            bodyNoHighway.put("instructions", true);
            bodyNoHighway.put("locale", "fr_FR");
            bodyNoHighway.put("points_encoded", true);
            bodyNoHighway.put("points_encoded_multiplier", 100_000);
            bodyNoHighway.put("details", List.of("road_class", "road_environment", "max_speed", "average_speed"));
            bodyNoHighway.put("snap_preventions", List.of("ferry"));
            bodyNoHighway.put("custom_model", customModel);
            bodyNoHighway.put("ch.disable", true);

            HttpEntity<String> requestNoHighway = new HttpEntity<>(mapper.writeValueAsString(bodyNoHighway), headers);
            String responseNoHighway = restTemplate.postForObject(graphhopperBaseUrl + "/route?key=", requestNoHighway, String.class);
            dto.setNoToll(responseNoHighway);

            // Requête 3 : économique (on peut l’améliorer plus tard, ici même que no highway)
            dto.setEconomical(responseNoHighway);

            return dto;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des itinéraires alternatifs", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors du calcul des itinéraires alternatifs", e);
        }
    }


    private String getCoordinates(String location) {
        String[] parts = location.split(",");
        if (parts.length >= 2) {
            try {
                Double.parseDouble(parts[0].trim());
                Double.parseDouble(parts[1].trim());
                return location;
            } catch (NumberFormatException e) {
                // Ce n'est pas déjà des coordonnées numériques.
            }
        }
        try {
            String encodedLocation = UriUtils.encode(location, StandardCharsets.UTF_8);
            String geocodeUrl = "https://graphhopper.com/api/1/geocode?q=" + encodedLocation
                    + "&locale=fr&key=" + graphhopperApiKey;
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(geocodeUrl, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode hits = root.path("hits");
            if (hits.isArray() && hits.size() > 0) {
                JsonNode firstHit = hits.get(0);
                JsonNode point = firstHit.path("point");
                double lat = point.path("lat").asDouble();
                double lng = point.path("lng").asDouble();
                return lat + "," + lng;
            } else {
                throw new RuntimeException("Aucun résultat de géocodage pour : " + location);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du géocodage pour : " + location, e);
        }
    }
}
