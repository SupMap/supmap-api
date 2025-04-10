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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "Gestion des itinéraires")
public class DirectionControllerImpl implements DirectionController {

    @Value("${graphhopper.api-key}")
    private String graphhopperApiKey;

    @Value("${graphhopper.base-url}")
    private String graphhopperBaseUrl;

    private final IncidentRepository incidentRepository;

    public DirectionControllerImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    @Operation(description = "Permet de récupérer un itinéraire entre deux points", summary = "Get Route")
    public String getDirection(String origin, String mode, String destination, String customModel) {
        String originCoordinates = getCoordinates(origin);
        String destinationCoordinates = getCoordinates(destination);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();

            if ("noToll".equalsIgnoreCase(customModel)) {
                List<List<Double>> points = List.of(
                        List.of(Double.parseDouble(originCoordinates.split(",")[1].trim()),
                                Double.parseDouble(originCoordinates.split(",")[0].trim())),
                        List.of(Double.parseDouble(destinationCoordinates.split(",")[1].trim()),
                                Double.parseDouble(destinationCoordinates.split(",")[0].trim()))
                );

                Map<String, Object> body = new HashMap<>();
                body.put("points", points);
                body.put("profile", mode);
                body.put("elevation", false);
                body.put("instructions", true);
                body.put("locale", "fr_FR");
                body.put("points_encoded", true);
                body.put("details", List.of("road_class", "average_speed"));
                body.put("ch.disable", true);
                body.put("custom_model", generateCustomModelFromIncidents(true));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                return restTemplate.postForObject(
                        graphhopperBaseUrl + "/route?key=" + graphhopperApiKey,
                        request,
                        String.class
                );
            } else {
                String url = graphhopperBaseUrl + "/route?"
                        + "point=" + originCoordinates
                        + "&point=" + destinationCoordinates
                        + "&profile=" + mode
                        + "&locale=fr_FR"
                        + "&points_encoded=true"
                        + "&instructions=true"
                        + "&details=road_class&details=average_speed"
                        + "&key=" + graphhopperApiKey;

                log.info("GET {}", url);
                return restTemplate.getForObject(url, String.class);
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
            ObjectMapper mapper = new ObjectMapper();

            List<List<Double>> points = List.of(
                    List.of(Double.parseDouble(originCoordinates.split(",")[1].trim()),
                            Double.parseDouble(originCoordinates.split(",")[0].trim())),
                    List.of(Double.parseDouble(destinationCoordinates.split(",")[1].trim()),
                            Double.parseDouble(destinationCoordinates.split(",")[0].trim()))
            );

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
            bodyAlternative.put("ch.disable", true);
            bodyAlternative.put("custom_model", generateCustomModelFromIncidents(false));

            Map<String, Object> bodyNoToll = new HashMap<>();
            bodyNoToll.put("points", points);
            bodyNoToll.put("profile", mode);
            bodyNoToll.put("elevation", false);
            bodyNoToll.put("instructions", true);
            bodyNoToll.put("locale", "fr_FR");
            bodyNoToll.put("points_encoded", true);
            bodyNoToll.put("details", List.of("road_class", "average_speed"));
            bodyNoToll.put("ch.disable", true);
            bodyNoToll.put("custom_model", generateCustomModelFromIncidents(true));

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> requestAlt = new HttpEntity<>(bodyAlternative, headers);
            String responseAlt = restTemplate.postForObject(
                    graphhopperBaseUrl + "/route?key=" + graphhopperApiKey,
                    requestAlt,
                    String.class
            );

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

            HttpEntity<Map<String, Object>> requestNoToll = new HttpEntity<>(bodyNoToll, headers);
            try {
                String responseNoToll = restTemplate.postForObject(
                        graphhopperBaseUrl + "/route?key=" + graphhopperApiKey,
                        requestNoToll,
                        String.class
                );
                dto.setNoToll(responseNoToll);
            } catch (Exception e) {
                dto.setNoToll(responseAlt);
            }

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
                // Si la conversion échoue, on suppose que c'est une adresse
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

    private Map<String, Object> generateCustomModelFromIncidents(Boolean noToll) {
        try {
            List<Incident> incidents = incidentRepository.findAll();

            ObjectMapper mapper = new ObjectMapper();
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
                polygonCoordinates.add(array(mapper, lon - delta, lat - delta)); // ferme le polygone

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

                // Règle sur la vitesse
                ObjectNode speedRule = mapper.createObjectNode();
                speedRule.put("if", "in_" + areaId);
                speedRule.put("multiply_by", multiplier);
                speedRules.add(speedRule);

                // Règle sur la priorité
                ObjectNode priorityRule = mapper.createObjectNode();
                priorityRule.put("if", "in_" + areaId);
                priorityRule.put("multiply_by", multiplier);
                priorityRules.add(priorityRule);

                areaIndex++;
            }

            // Construction du FeatureCollection global pour les areas
            ObjectNode areasNode = mapper.createObjectNode();
            areasNode.put("type", "FeatureCollection");
            areasNode.set("features", allFeatures);

            // Custom model final
            ObjectNode customModel = mapper.createObjectNode();
            customModel.set("areas", areasNode);
            customModel.set("speed", speedRules);
            customModel.set("priority", priorityRules);

            // Ajout règle noToll si demandé
            if (noToll) {
                ObjectNode motorwayRule = mapper.createObjectNode();
                motorwayRule.put("if", "road_class == MOTORWAY");
                motorwayRule.put("multiply_by", 0.0);
                priorityRules.add(motorwayRule);

                // Met à jour la priority avec les 2 types de règles
                customModel.set("priority", priorityRules);
            }

            return mapper.convertValue(customModel, Map.class);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du custom model incidents + autoroutes", e);
            return null;
        }
    }

    // Utilitaire : tableau [lon, lat]
    private ArrayNode array(ObjectMapper mapper, double lon, double lat) {
        ArrayNode coord = mapper.createArrayNode();
        coord.add(lon);
        coord.add(lat);
        return coord;
    }



}




//{
//        "points": [
//        [
//        0.699346, 47.420072
//        ],
//        [
//        0.702779, 47.405116
//        ]
//        ],
//        "snap_preventions": [
//        "motorway",
//        "ferry",
//        "tunnel"
//        ],
//        "details": [
//        "road_class",
//        "surface"
//        ],
//        "custom_model": {
//        "priority": [
//        {
//        "if": "road_access == PRIVATE",
//        "multiply_by": "0"
//        },
//        {
//        "if": "road_class == MOTORWAY",
//        "multiply_by": "0"
//        }
//        ]
//        },
//        "ch.disable": true,
//        "profile": "car",
//        "locale": "en",
//        "instructions": true,
//        "calc_points": true,
//        "points_encoded": false
//        }