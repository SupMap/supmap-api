package fr.supmap.supmapapi.controller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
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
        DirectionsDto dto = new DirectionsDto();

        try {
            ObjectMapper mapper = new ObjectMapper();

            List<List<Double>> points = List.of(
                    List.of(
                            Double.parseDouble(originCoordinates.split(",")[1].trim()),
                            Double.parseDouble(originCoordinates.split(",")[0].trim())
                    ),
                    List.of(
                            Double.parseDouble(destinationCoordinates.split(",")[1].trim()),
                            Double.parseDouble(destinationCoordinates.split(",")[0].trim())
                    )
            );

            // ---- Requête pour fastest & economical (alternative_route) ----
            Map<String, Object> bodyAlternative = new HashMap<>();
            bodyAlternative.put("points", points);
            bodyAlternative.put("profile", mode);
            bodyAlternative.put("elevation", false);
            bodyAlternative.put("instructions", true);
            bodyAlternative.put("locale", "fr_FR");
            bodyAlternative.put("points_encoded", true);
            bodyAlternative.put("details", List.of("road_class", "average_speed"));
            bodyAlternative.put("algorithm", "alternative_route");
            bodyAlternative.put("alternative_route.max_paths", 2);

            // ---- Requête pour noToll (custom_model interdit les autoroutes) ----
            Map<String, Object> priorityRule = Map.of(
                    "if", "road_class == MOTORWAY",
                    "multiply_by", 0.0
            );
            Map<String, Object> customModel = Map.of(
                    "priority", List.of(priorityRule)
            );

            Map<String, Object> bodyNoToll = new HashMap<>();
            bodyNoToll.put("points", points);
            bodyNoToll.put("profile", mode);
            bodyNoToll.put("elevation", false);
            bodyNoToll.put("instructions", true);
            bodyNoToll.put("locale", "fr_FR");
            bodyNoToll.put("points_encoded", true);
            bodyNoToll.put("details", List.of("road_class", "average_speed"));
            bodyNoToll.put("ch.disable", true);
            bodyNoToll.put("custom_model", customModel);

            WebClient webClient = WebClient.builder()
                    .baseUrl(graphhopperBaseUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "application/json")
                    .build();

            // --- POST fastest + economical ---
            String responseAlt = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/route")
                            .queryParam("key", graphhopperApiKey)
                            .build())
                    .body(BodyInserters.fromValue(bodyAlternative))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode altJson = mapper.readTree(responseAlt);
            JsonNode paths = altJson.path("paths");

            if (paths.isArray() && paths.size() >= 2) {
                // fastest
                ObjectNode fastestNode = altJson.deepCopy();
                fastestNode.set("paths", mapper.createArrayNode().add(paths.get(0)));
                dto.setFastest(mapper.writeValueAsString(fastestNode));

                // economical
                ObjectNode economicalNode = altJson.deepCopy();
                economicalNode.set("paths", mapper.createArrayNode().add(paths.get(1)));
                dto.setEconomical(mapper.writeValueAsString(economicalNode));
            } else {
                dto.setFastest(responseAlt);
                dto.setEconomical(responseAlt);
            }

            // --- POST noToll ---
            String responseNoToll = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/route")
                            .queryParam("key", graphhopperApiKey)
                            .build())
                    .body(BodyInserters.fromValue(bodyNoToll))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            dto.setNoToll(responseNoToll);

            return dto;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des itinéraires", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du calcul des itinéraires", e);
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
