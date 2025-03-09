package fr.supmap.supmapapi.controller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.supmap.supmapapi.controller.DirectionController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
public class DirectionControllerImpl implements DirectionController {

    @Value("${graphhopper.api-key}")
    private String graphhopperApiKey;

    /**
     * @param origin      peut être soit au format "lat,lon" (ex : "48.8566,2.3522")
     *                    soit une adresse/text (ex : "Paris")
     * @param mode        "car", "bike", "foot", etc.
     * @param destination idem que origin (coordonnées ou texte)
     * @return            la réponse JSON brute de l'API GraphHopper
     */
    @Override
    public String getDirections(String origin, String mode, String destination) {
        // Si les paramètres ne sont pas déjà des coordonnées, on effectue le géocodage.
        String originCoordinates = getCoordinates(origin);
        String destinationCoordinates = getCoordinates(destination);

        String url = "https://graphhopper.com/api/1/route?"
                + "point=" + originCoordinates
                + "&point=" + destinationCoordinates
                + "&vehicle=" + mode
                + "&locale=fr"
                + "&instructions=true"
                + "&calc_points=true"
                + "&key=" + graphhopperApiKey;

        RestTemplate restTemplate = new RestTemplate();
        log.info("GET " + url);
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * Retourne les coordonnées au format "lat,lon" pour une location donnée.
     * Si location contient déjà une virgule, on considère que c'est déjà des coordonnées.
     * Sinon, on interroge l'API de géocodage GraphHopper.
     *
     * @param location adresse ou coordonnées
     * @return chaîne de caractères "lat,lon"
     */
    private String getCoordinates(String location) {
        if(location.contains(",")) {
            // On considère que c'est déjà sous forme "lat,lon"
            return location;
        }
        try {
            // Encodage de la chaîne pour l'URL
            String encodedLocation = UriUtils.encode(location, StandardCharsets.UTF_8);
            String geocodeUrl = "https://graphhopper.com/api/1/geocode?q=" + encodedLocation
                    + "&locale=fr&key=" + graphhopperApiKey;
            RestTemplate restTemplate = new RestTemplate();
//            log.info("Geocoding GET " + geocodeUrl);
            String response = restTemplate.getForObject(geocodeUrl, String.class);

            // Parser la réponse JSON pour extraire la première correspondance
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
                throw new RuntimeException("Aucun résultat de géocodage pour: " + location);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du géocodage pour: " + location, e);
        }
    }
}
