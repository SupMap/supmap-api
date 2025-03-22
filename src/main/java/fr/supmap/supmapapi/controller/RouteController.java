package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.entity.table.Route;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface RouteController {

    @PostMapping("/routes")
    Route createRoute(@RequestBody Route route);

    @GetMapping("/routes/{id}")
    Route getRoute(@PathVariable("id") Integer id);
}
