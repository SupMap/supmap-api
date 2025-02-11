package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.DirectionController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
public class DirectionControllerImpl implements DirectionController {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Override
    public String getDirections(String origin, String mode, String destination) {
        // Construire l'URL pour l'appel à l'API Google Directions
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + origin + "&mode=" + mode + "&destination=" + destination + "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        log.info("GET " + url);
        // Effectuer la requête GET et retourner la réponse JSON
        return restTemplate.getForObject(url, String.class);
    }
}
