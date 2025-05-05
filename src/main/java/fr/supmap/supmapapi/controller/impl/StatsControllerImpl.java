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
import java.util.LinkedHashMap;
import java.util.Map;

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
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    @Override
    @Operation(description = "Permet de récupérer les statistiques", summary = "Get Stats")
    public StatsDto getStats() {
        User user = GetUserAuthenticated();
        if (!"Administrateur".equals(user.getRole().getName())) {
            throw new NotFoundException("Ressource non trouvée");
        }

        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

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

        Map<Integer, Long> incidentsByHour = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) {
            Instant start = today.atStartOfDay(zone).plusHours(h).toInstant();
            Instant end   = today.atStartOfDay(zone).plusHours(h + 1).toInstant();
            long count = incidentRepository.countByCreatedAtBetween(start, end);
            incidentsByHour.put(h, count);
        }
        stats.setIncidentsPerHour(incidentsByHour);

        Map<Integer, Long> tripsByDay = new LinkedHashMap<>();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        int daysInMonth = today.lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = firstOfMonth.plusDays(d - 1);
            Instant start = date.atStartOfDay(zone).toInstant();
            Instant end   = date.plusDays(1).atStartOfDay(zone).toInstant();
            long count = routeRepository.countByCalculatedAtBetween(start, end);
            tripsByDay.put(d, count);
        }
        stats.setTripsPerDay(tripsByDay);

        return stats;
    }

    private Instant startOfDay() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant startOfWeek() {
        return LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

    private Instant startOfMonth() {
        return LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }

    private Instant startOfYear() {
        return LocalDate.now()
                .withDayOfYear(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }
}
