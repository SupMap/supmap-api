package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.RouteController;
import fr.supmap.supmapapi.model.entity.table.Route;
import fr.supmap.supmapapi.repository.RouteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Gestion des routes")
public class RouteControllerImpl implements RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @Override
    @Operation(description = "Permet de créer une route", summary = "Create Route")
    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    @Override
    @Operation(description = "Permet de récuperer une route", summary = "Get Routes (Not yet implemented)")
    public Route getRoute(Integer id) {
        return routeRepository.findById(id).orElse(null);
    }
}
