package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.DirectionsDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface DirectionController {

    @GetMapping("/direction")
    String getDirection(@RequestParam(value="origin") String origin,
                         @RequestParam(value="mode", defaultValue="car") String mode,
                         @RequestParam(value="destination") String destination);

    @GetMapping("/directions")
    DirectionsDto getDirections(@RequestParam("origin") String origin,
                                @RequestParam("mode") String mode,
                                @RequestParam("destination") String destination);
}
