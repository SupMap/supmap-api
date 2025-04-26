package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.DirectionController;
import fr.supmap.supmapapi.controller.RouteController;
import fr.supmap.supmapapi.model.dto.RouteDto;
import fr.supmap.supmapapi.model.entity.table.Route;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.RouteRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.utils.GeoUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;


/**
 * The type Route controller.
 */
@RestController
@Tag(name = "Gestion des routes")
public class RouteControllerImpl implements RouteController {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final DirectionController directionController;

    /**
     * Instantiates a new Route controller.
     *
     * @param routeRepository     the route repository
     * @param userRepository      the user repository
     * @param directionController the direction controller
     */
    public RouteControllerImpl(RouteRepository routeRepository, UserRepository userRepository, DirectionController directionController) {
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.directionController = directionController;
    }

    private User GetUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        int userId = Integer.parseInt(authentication.getName());

        return userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    @Override
    @Operation(description = "Permet de créer une route", summary = "Create Route")
    public void createRoute(RouteDto routeDto) {
        User user = GetUserAuthenticated();

        this.desactivateRoute(user);

        Route route = new Route();
        route.setUser(user);
        route.setTotalDuration(routeDto.getTotalDuration());
        route.setTotalDistance(routeDto.getTotalDistance());
        route.setCustomModel(routeDto.getCustomModel());
        route.setMode(routeDto.getMode());
        route.setStartLocation(GeoUtils.parsePoint(routeDto.getStartLocation()));
        route.setEndLocation(GeoUtils.parsePoint(routeDto.getEndLocation()));
        route.setRouteGeometry(GeoUtils.decodePolylineToLineString(routeDto.getRoute()));
        route.setCalculatedAt(Instant.now());
        route.setActive(true);

        routeRepository.save(route);
    }

    private void desactivateRoute(User user) {
        Route route = routeRepository.findRouteByUserIdAndActive(user.getId(), true);
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune route trouvée pour cet utilisateur");
        }
        route.setActive(false);
        routeRepository.save(route);
    }


    @Override
    @Operation(description = "Permet de récuperer une route active", summary = "Get Routes")
    public String getUserRoute(String origin) {
        User user = GetUserAuthenticated();
        Route route = routeRepository.findRouteByUserIdAndActive(user.getId(), true);
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune route trouvée pour cet utilisateur");
        }

        String destination = route.getEndLocation().getY() + "," + route.getEndLocation().getX();

        return directionController.getDirection(origin,
                                                route.getMode(),
                                                destination,
                                                route.getCustomModel());
    }


}
