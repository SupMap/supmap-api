package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.DirectionsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Direction controller.
 */
public interface DirectionController {

    /**
     * Gets direction.
     *
     * @param origin      the origin
     * @param mode        the mode
     * @param destination the destination
     * @param customModel the custom model
     * @return the direction
     */
    @GetMapping("/direction")
    String getDirection(@RequestParam(value = "origin") String origin,
                        @RequestParam(value = "mode", defaultValue = "car") String mode,
                        @RequestParam(value = "destination") String destination,
                        @RequestParam(value = "custom-model", required = false) String customModel);

    /**
     * Gets directions.
     *
     * @param origin      the origin
     * @param mode        the mode
     * @param destination the destination
     * @return the directions
     */
    @GetMapping("/directions")
    DirectionsDto getDirections(@RequestParam(value = "origin") String origin,
                                @RequestParam(value = "mode", defaultValue = "car") String mode,
                                @RequestParam(value = "destination") String destination);
}
