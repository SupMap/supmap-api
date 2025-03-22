package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.RouteController;
import fr.supmap.supmapapi.model.entity.table.Route;
import fr.supmap.supmapapi.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteControllerImpl implements RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @Override
    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public Route getRoute(Integer id) {
        return routeRepository.findById(id).orElse(null);
    }
}
