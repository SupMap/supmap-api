package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.UserController;
import fr.supmap.supmapapi.model.dto.RouteDto;
import fr.supmap.supmapapi.model.dto.userDtos.UserMinimalInfoDto;
import fr.supmap.supmapapi.model.dto.userDtos.UserUpdateDto;
import fr.supmap.supmapapi.model.entity.table.Route;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.RouteRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.exceptions.NotAuthorizeException;
import fr.supmap.supmapapi.services.exceptions.NotFoundException;
import fr.supmap.supmapapi.utils.GeoUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static fr.supmap.supmapapi.services.PasswordManager.hashPassword;

/**
 * The type {@code UserControllerImpl} is an implementation of the {@link UserController} interface.
 */
@RestController
@Tag(name = "Gestion du User")
public class UserControllerImpl implements UserController {

    private final Logger log = LoggerFactory.getLogger(UserControllerImpl.class);
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;

    public UserControllerImpl(UserRepository userRepository, RouteRepository routeRepository) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
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
    @Operation(description = "Permet de lister tous les utilisateurs (désactivé)", summary = "Get All Users (disabled)")
    public List<User> getAll() {
        throw new NotAuthorizeException("Ressource désactivée");
    }

    @Override
    @Operation(description = "Permet de récupérer les informations d'un utilisateur spécifique (il faut etre admin)", summary = "Get User")
    public UserMinimalInfoDto getUser(Integer userId) {
        log.info("GET /users/{}", userId);

        User user = GetUserAuthenticated();
        if (!user.getRole().getName().equals("Administrateur")) {
            throw new NotFoundException("Ressource non trouvée");
        }

        return UserMinimalInfoDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .secondName(user.getSecondName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .contributionNumber(user.getContribution())
                .build();
    }

    @Override
    @Operation(description = "Permet de récupérer les informations de l'utilisateur connecté", summary = "Get User Info")
    public UserMinimalInfoDto getUserInfo() {
        User user = GetUserAuthenticated();

        log.info("GET /users/info - User: {}", user.getUsername());

        return UserMinimalInfoDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .secondName(user.getSecondName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .contributionNumber(user.getContribution())
                .build();
    }

    @Transactional
    @Operation(description = "Permet de modifier les infos d'un user", summary = "Update user")
    public UserMinimalInfoDto updateUser(UserUpdateDto userUpdateDto) {
        User user = GetUserAuthenticated();

        userUpdateDto.getUsername().ifPresent(user::setUsername);
        userUpdateDto.getPassword().ifPresent(password -> user.setPasswordHash(hashPassword(password)));
        userUpdateDto.getName().ifPresent(user::setName);
        userUpdateDto.getSecondName().ifPresent(user::setSecondName);
        userUpdateDto.getEmail().ifPresent(user::setEmail);

        User updatedUser = userRepository.save(user);

        log.info("PATCH /users - User: {}", updatedUser.getUsername());

        return UserMinimalInfoDto.builder()
                .username(updatedUser.getUsername())
                .name(updatedUser.getName())
                .secondName(updatedUser.getSecondName())
                .email(updatedUser.getEmail())
                .build();
    }

    @Override
    @Operation(description = "Permet de récupérer les itinéraires d'un utilisateur", summary = "Get User Routes")
    public List<RouteDto> getAllRoutes() {
        User user = GetUserAuthenticated();
        log.info("GET /users/{}/routes", user.getId());
        List<Route> routes = routeRepository.findByUserId(user.getId());
        if (routes.isEmpty()) {
            throw new NotFoundException("Aucun itinéraire trouvé pour cet utilisateur");
        }
        return routes.stream()
                .map(route -> new RouteDto(
                        GeoUtils.encodeLineStringToPolyline(route.getRouteGeometry()),
                        route.getTotalDuration(),
                        route.getTotalDistance(),
                        route.getCustomModel(),
                        route.getMode(),
                        route.getStartLocation().toString(),
                        route.getEndLocation().toString(),
                        route.getActive()
                )).toList();
    }
}