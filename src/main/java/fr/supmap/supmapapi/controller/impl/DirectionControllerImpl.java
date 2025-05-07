package fr.supmap.supmapapi.controller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.supmap.supmapapi.controller.DirectionController;
import fr.supmap.supmapapi.model.dto.DirectionsDto;
import fr.supmap.supmapapi.model.entity.table.Incident;
import fr.supmap.supmapapi.repository.IncidentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type {@code DirectionControllerImpl} is an implementation of the {@link DirectionController} interface.
 */
@Slf4j
@RestController
@Tag(name = "Gestion des itinéraires")
public class DirectionControllerImpl implements DirectionController {

    private final IncidentRepository incidentRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${graphhopper.api-key}")
    private String graphhopperApiKey;
    @Value("${graphhopper.base-url}")
    private String graphhopperBaseUrl;


    public DirectionControllerImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    @Operation(description = "Permet de récupérer un itinéraire entre deux points", summary = "Get Route")
    public String getDirection(String origin, String mode, String destination, String customModelType) {
        String originCoordinates = getCoordinates(origin);
        String destinationCoordinates = getCoordinates(destination);

        try {
            List<List<Double>> points = List.of(
                    List.of(getLng(originCoordinates), getLat(originCoordinates)),
                    List.of(getLng(destinationCoordinates), getLat(destinationCoordinates))
            );

            Map<String, Object> body = buildRequestBody(points, mode, false, true);

            if ("noToll".equalsIgnoreCase(customModelType)) {
                body.put("custom_model", generateCustomModelFromIncidents(true));
            } else {
                body.put("custom_model", generateCustomModelFromIncidents(false));
            }

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            try {
                return restTemplate.postForObject(
                        graphhopperBaseUrl + "/route?key=" + graphhopperApiKey,
                        request,
                        String.class
                );
            } catch (Exception e) {
                return this.getSecureDirection(origin, mode, destination, false);
            }

        } catch (Exception e) {
            log.error("Erreur dans getDirection", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la récupération de l'itinéraire", e);
        }
    }

    @Override
    @Operation(description = "Permet de récupérer 3 itinéraires alternatifs entre deux points", summary = "Get Alternative Routes")
    public DirectionsDto getDirections(String origin, String mode, String destination) {
        String originCoordinates = getCoordinates(origin);
        String destinationCoordinates = getCoordinates(destination);
        DirectionsDto dto = new DirectionsDto();

        try {
            List<List<Double>> points = List.of(
                    List.of(getLng(originCoordinates), getLat(originCoordinates)),
                    List.of(getLng(destinationCoordinates), getLat(destinationCoordinates))
            );

            Map<String, Object> bodyAlternative = buildRequestBody(points, mode, true, true);
            bodyAlternative.put("custom_model", generateCustomModelFromIncidents(false));

            String responseAlt = "";
            try {
                HttpEntity<Map<String, Object>> requestAlt = new HttpEntity<>(bodyAlternative, createHeaders());
                responseAlt = restTemplate.postForObject(
                        graphhopperBaseUrl + "/route?key=" + graphhopperApiKey,
                        requestAlt,
                        String.class
                );
            } catch (Exception e) {
                responseAlt = this.getSecureDirection(origin, mode, destination, true);
            }

            JsonNode altJson = mapper.readTree(responseAlt);
            JsonNode paths = altJson.path("paths");

            if (paths.isArray() && paths.size() >= 2) {
                ObjectNode fastestNode = altJson.deepCopy();
                fastestNode.set("paths", mapper.createArrayNode().add(paths.get(0)));
                dto.setFastest(mapper.writeValueAsString(fastestNode));

                ObjectNode economicalNode = altJson.deepCopy();
                economicalNode.set("paths", mapper.createArrayNode().add(paths.get(1)));
                dto.setEconomical(mapper.writeValueAsString(economicalNode));
            } else {
                dto.setFastest(responseAlt);
                dto.setEconomical(responseAlt);
            }

            Map<String, Object> bodyNoToll = buildRequestBody(points, mode, false, true);
            bodyNoToll.put("custom_model", generateCustomModelFromIncidents(true));


            try {
                HttpEntity<Map<String, Object>> requestNoToll = new HttpEntity<>(bodyNoToll, createHeaders());
                String responseNoToll = restTemplate.postForObject(
                        graphhopperBaseUrl + "/route?key=" + graphhopperApiKey,
                        requestNoToll,
                        String.class
                );
                dto.setNoToll(responseNoToll);
            } catch (Exception e) {
                ObjectNode fastestNode = altJson.deepCopy();
                fastestNode.set("paths", mapper.createArrayNode().add(paths.get(0)));
                dto.setNoToll(mapper.writeValueAsString(fastestNode));
            }

            return dto;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des itinéraires", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du calcul des itinéraires", e);
        }
    }


    private String getSecureDirection(String origin, String mode, String destination, Boolean isAlternative) {
        String originCoordinates = getCoordinates(origin);
        String destinationCoordinates = getCoordinates(destination);

        try {
            List<List<Double>> points = List.of(
                    List.of(getLng(originCoordinates), getLat(originCoordinates)),
                    List.of(getLng(destinationCoordinates), getLat(destinationCoordinates))
            );

            Map<String, Object> body = buildRequestBody(points, mode, isAlternative, false);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            return restTemplate.postForObject(
                    graphhopperBaseUrl + "/route?key=" + graphhopperApiKey,
                    request,
                    String.class
            );
        } catch (Exception e) {
            log.error("Erreur dans getSecureDirection", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du calcul de l'itinéraire de secours", e);
        }
    }

    private Map<String, Object> buildRequestBody(List<List<Double>> points, String mode, boolean isAlternative, boolean isCustomModel) {
        Map<String, Object> body = new HashMap<>();
        body.put("points", points);
        body.put("profile", mode);
        body.put("elevation", false);
        body.put("instructions", true);
        body.put("locale", "fr_FR");
        body.put("points_encoded", true);
        body.put("details", List.of("road_class", "average_speed"));
        if (isCustomModel) {
            body.put("ch.disable", true);
        }
        if (isAlternative) {
            body.put("algorithm", "alternative_route");
            body.put("alternative_route.max_paths", 2);
        }
        return body;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private double getLat(String coordinates) {
        return Double.parseDouble(coordinates.split(",")[0].trim());
    }

    private double getLng(String coordinates) {
        return Double.parseDouble(coordinates.split(",")[1].trim());
    }

    private String getCoordinates(String location) {
        String[] parts = location.split(",");
        if (parts.length >= 2) {
            try {
                Double.parseDouble(parts[0].trim());
                Double.parseDouble(parts[1].trim());
                return location;
            } catch (NumberFormatException e) {
                // Si la conversion échoue, on suppose que c'est une adresse
            }
        }
        try {
            String encodedLocation = UriUtils.encode(location, StandardCharsets.UTF_8);
            String geocodeUrl = "https://graphhopper.com/api/1/geocode?q=" + encodedLocation +
                    "&locale=fr&key=" + graphhopperApiKey;
            String response = restTemplate.getForObject(geocodeUrl, String.class);

            JsonNode root = mapper.readTree(response);
            JsonNode hits = root.path("hits");
            if (hits.isArray() && hits.size() > 0) {
                JsonNode point = hits.get(0).path("point");
                return point.path("lat").asDouble() + "," + point.path("lng").asDouble();
            } else {
                throw new RuntimeException("Aucun résultat de géocodage pour : " + location);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du géocodage pour : " + location, e);
        }
    }

    private Map<String, Object> generateCustomModelFromIncidents(Boolean noToll) {
        try {
            List<Incident> incidents = incidentRepository.findByExpirationDateAfter(java.time.Instant.now());
            ArrayNode allFeatures = mapper.createArrayNode();
            ArrayNode speedRules = mapper.createArrayNode();
            ArrayNode priorityRules = mapper.createArrayNode();

            int areaIndex = 0;
            for (Incident incident : incidents) {
                String areaId = "custom" + areaIndex;
                double lon = incident.getLocation().getX();
                double lat = incident.getLocation().getY();
                double delta = 0.00005;

                ArrayNode polygonCoordinates = mapper.createArrayNode();
                polygonCoordinates.add(array(mapper, lon - delta, lat - delta));
                polygonCoordinates.add(array(mapper, lon + delta, lat - delta));
                polygonCoordinates.add(array(mapper, lon + delta, lat + delta));
                polygonCoordinates.add(array(mapper, lon - delta, lat + delta));
                polygonCoordinates.add(array(mapper, lon - delta, lat - delta));

                ArrayNode polygon = mapper.createArrayNode();
                polygon.add(polygonCoordinates);

                ObjectNode geometry = mapper.createObjectNode();
                geometry.put("type", "Polygon");
                geometry.set("coordinates", polygon);

                ObjectNode feature = mapper.createObjectNode();
                feature.put("type", "Feature");
                feature.put("id", areaId);
                feature.set("geometry", geometry);
                allFeatures.add(feature);

                double multiplier = incident.getType().getWeight();

                ObjectNode speedRule = mapper.createObjectNode();
                speedRule.put("if", "in_" + areaId);
                speedRule.put("multiply_by", multiplier);
                speedRules.add(speedRule);

                ObjectNode priorityRule = mapper.createObjectNode();
                priorityRule.put("if", "in_" + areaId);
                priorityRule.put("multiply_by", multiplier);
                priorityRules.add(priorityRule);

                areaIndex++;
            }

            ObjectNode areasNode = mapper.createObjectNode();
            areasNode.put("type", "FeatureCollection");
            areasNode.set("features", allFeatures);

            ObjectNode customModel = mapper.createObjectNode();
            customModel.set("areas", areasNode);
            customModel.set("speed", speedRules);
            customModel.set("priority", priorityRules);

            if (noToll) {
                ObjectNode motorwayRule = mapper.createObjectNode();
                motorwayRule.put("if", "road_class == MOTORWAY");
                motorwayRule.put("multiply_by", 0.0);
                priorityRules.add(motorwayRule);
                customModel.set("priority", priorityRules);
            }

            return mapper.convertValue(customModel, Map.class);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du custom model", e);
            return null;
        }
    }

    private ArrayNode array(ObjectMapper mapper, double lon, double lat) {
        ArrayNode array = mapper.createArrayNode();
        array.add(lon);
        array.add(lat);
        return array;
    }
}
