package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.RouteDto;
import fr.supmap.supmapapi.model.entity.table.Route;
import org.springframework.web.bind.annotation.*;

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
    String getUserRoute(@RequestParam(value="origin", required=false) String origin);
}
