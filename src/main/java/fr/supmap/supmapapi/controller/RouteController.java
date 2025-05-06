package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.RouteDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Route controller.
 */
public interface RouteController {

    /**
     * Create route.
     *
     * @param route the route
     */
    @PostMapping("/route")
    void createRoute(@RequestBody RouteDto route);

    /**
     * Gets user route.
     *
     * @param origin the origin
     * @return the user route
     */
    @GetMapping("/user/route")
    String getUserRoute(@RequestParam(value = "origin", required = false) String origin);
}
