package fr.supmap.supmapapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface DirectionController {

    @GetMapping("/directions")
    String getDirections(@RequestParam(value="origin") String origin,
                         @RequestParam(value="mode", defaultValue="car") String mode,
                         @RequestParam(value="destination") String destination);
}
