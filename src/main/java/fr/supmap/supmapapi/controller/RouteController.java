package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.RouteDto;
import fr.supmap.supmapapi.model.entity.table.Route;
import org.springframework.web.bind.annotation.*;

public interface RouteController {

    @PostMapping("/route")
    void createRoute(@RequestBody RouteDto route);

    @GetMapping("/user/route")
    String getUserRoute(@RequestParam("origin") String origin);
}
