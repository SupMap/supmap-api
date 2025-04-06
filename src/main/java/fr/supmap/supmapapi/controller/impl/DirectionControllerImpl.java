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

            // Construction de la liste des points au format [longitude, latitude]
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

            // Préparation des corps de requêtes pour POST
            // Corps identique pour "sans autoroute" et "économique", à l'exception du paramètre alternative pour le dernier.
            Map<String, Object> priorityRule = new HashMap<>();
            priorityRule.put("if", "road_class == MOTORWAY");
            priorityRule.put("multiply_by", 0.0);

            Map<String, Object> customModel = new HashMap<>();
            customModel.put("priority", List.of(priorityRule));

            // Corps pour itinéraire sans autoroute
            Map<String, Object> bodyNoHighway = new HashMap<>();
            bodyNoHighway.put("points", points);
            bodyNoHighway.put("profile", mode);
            bodyNoHighway.put("elevation", true);
            bodyNoHighway.put("instructions", true);
            bodyNoHighway.put("locale", "fr_FR");
            bodyNoHighway.put("points_encoded", true);
            bodyNoHighway.put("points_encoded_multiplier", 100000);
            bodyNoHighway.put("details", List.of("road_class", "road_environment", "max_speed", "average_speed"));
            bodyNoHighway.put("snap_preventions", List.of("ferry"));
            bodyNoHighway.put("custom_model", customModel);
            bodyNoHighway.put("ch.disable", true);

            // Corps pour itinéraire économique avec alternatives (2 alternatives demandées)
            Map<String, Object> bodyEconomical = new HashMap<>(bodyNoHighway);
            bodyEconomical.put("algorithm", "alternative_route");
            bodyEconomical.put("alternative_route.max_paths", 2);
            bodyEconomical.remove("custom_model");

            WebClient webClient = WebClient.builder()
                    .baseUrl(graphhopperBaseUrl)
                    .defaultHeader("Content-Type", "application/json; charset=UTF-8")
                    .defaultHeader("Accept", "application/json")
                    .build();

            String classicUrl = graphhopperBaseUrl + "/route?" +
                    "point=" + originCoordinates +
                    "&point=" + destinationCoordinates +
                    "&profile=" + mode +
                    "&key=" + graphhopperApiKey;
            String classicResponse = webClient.get()
                    .uri(classicUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            dto.setFastest(classicResponse);

            // --- Itinéraire sans autoroute (POST) ---
            String noHighwayResponse = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/route")
                            .queryParam("key", graphhopperApiKey)
                            .build())
                    .body(BodyInserters.fromValue(bodyNoHighway))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            dto.setNoToll(noHighwayResponse);

            // --- Itinéraire économique (POST) avec alternatives ---
            String economicalResponseFull = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/route")
                            .queryParam("key", graphhopperApiKey)
                            .build())
                    .body(BodyInserters.fromValue(bodyEconomical))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // On parse la réponse et on sélectionne la deuxième alternative, le but après est juste de regrouper le plus rapide et l'économique ensemble.
            JsonNode economicalJson = mapper.readTree(economicalResponseFull);
            JsonNode paths = economicalJson.path("paths");
            if (paths.isArray() && paths.size() > 1) {
                JsonNode secondPath = paths.get(1);
                ((ObjectNode) economicalJson).set("paths", mapper.createArrayNode());
                ((ObjectNode) economicalJson).withArray("paths").add(secondPath);
                dto.setEconomical(economicalJson.toString());
            }

            return dto;
        } catch (Exception e) {
            log.error("Erreur lors du calcul des itinéraires alternatifs", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du calcul des itinéraires alternatifs", e);
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
