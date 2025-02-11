package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.DirectionController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class DirectionControllerImpl implements DirectionController {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Override
    public String getDirections(@RequestParam(value="origin", defaultValue="Disneyland") String origin,
                                @RequestParam(value="destination", defaultValue="Universal Studios Hollywood") String destination) {
        // Construire l'URL pour l'appel à l'API Google Directions
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + origin + "&destination=" + destination + "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        // Effectuer la requête GET et retourner la réponse JSON
        return restTemplate.getForObject(url, String.class);
    }
}
