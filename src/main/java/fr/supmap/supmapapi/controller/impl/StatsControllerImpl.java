package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.StatsController;
import fr.supmap.supmapapi.model.dto.StatsDto;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.IncidentRepository;
import fr.supmap.supmapapi.repository.RouteRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;


@RestController
@Tag(name = "Gestion des statistiques")
public class StatsControllerImpl implements StatsController {

    private final RouteRepository routeRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public StatsControllerImpl(RouteRepository routeRepository, IncidentRepository incidentRepository, UserRepository userRepository) {
        this.routeRepository = routeRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
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
    @Operation(description = "Permet de récupérer les statistiques", summary = "Get Stats")
    public StatsDto getStats() {
        User user = GetUserAuthenticated();

        if (!user.getRole().getName().equals("Administrateur")) {
            throw new NotFoundException("Ressource non trouvée");
        }

        Instant now = Instant.now();
        StatsDto stats = new StatsDto();

        stats.setOngoingTrips((long) routeRepository.findByActive(true).size());
        stats.setTripsToday(routeRepository.countByCalculatedAtBetween(startOfDay(), now));
        stats.setTripsThisWeek(routeRepository.countByCalculatedAtBetween(startOfWeek(), now));
        stats.setTripsThisMonth(routeRepository.countByCalculatedAtBetween(startOfMonth(), now));
        stats.setTripsThisYear(routeRepository.countByCalculatedAtBetween(startOfYear(), now));

        stats.setOngoingIncidents(incidentRepository.countByExpirationDateAfter(now));
        stats.setIncidentsToday(incidentRepository.countByCreatedAtBetween(startOfDay(), now));
        stats.setIncidentsThisWeek(incidentRepository.countByCreatedAtBetween(startOfWeek(), now));
        stats.setIncidentsThisMonth(incidentRepository.countByCreatedAtBetween(startOfMonth(), now));
        stats.setIncidentsThisYear(incidentRepository.countByCreatedAtBetween(startOfYear(), now));

        stats.setAverageTripDuration(routeRepository.averageTotalDuration());
        stats.setAverageTripDistance(routeRepository.averageTotalDistance());

        return stats;
    }

    private Instant startOfDay() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant startOfWeek() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant startOfMonth() {
        return LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant startOfYear() {
        return LocalDate.now().withDayOfYear(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
