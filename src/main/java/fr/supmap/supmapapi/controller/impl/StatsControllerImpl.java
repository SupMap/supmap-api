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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The type {@code StatsControllerImpl} is an implementation of the {@link StatsController} interface.
 */
@RestController
@Tag(name = "Gestion des statistiques")
public class StatsControllerImpl implements StatsController {

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RouteRepository routeRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public StatsControllerImpl(RouteRepository routeRepository,
                               IncidentRepository incidentRepository,
                               UserRepository userRepository) {
        this.routeRepository = routeRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    private User getUserAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        int userId = Integer.parseInt(auth.getName());
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    @Override
    @Operation(description = "Permet de récupérer les statistiques", summary = "Get Stats")
    public StatsDto getStats() {
        User user = getUserAuthenticated();
        if (!"Administrateur".equals(user.getRole().getName())) {
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

        stats.setTripsPerDay(buildTripsLast30DaysStats(ZONE));
        stats.setIncidentsPerHour(buildIncidentsPerHourStats(ZONE));

        return stats;
    }

    private Map<String, Long> buildTripsLast30DaysStats(ZoneId zone) {
        Map<String, Long> tripsByDay = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(zone);

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Instant start = date.atStartOfDay(zone).toInstant();
            Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();
            long count = routeRepository.countByCalculatedAtBetween(start, end);
            tripsByDay.put(date.format(DAY_FORMATTER), count);
        }
        return tripsByDay;
    }

    private Map<String, Long> buildIncidentsPerHourStats(ZoneId zone) {
        Map<String, Long> incidentsByHour = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(zone);
        ZonedDateTime dayStartZdt = today.atStartOfDay(zone);

        for (int h = 0; h < 24; h++) {
            ZonedDateTime slotStart = dayStartZdt.plusHours(h);
            Instant start = slotStart.toInstant();
            Instant end = slotStart.plusHours(1).toInstant();
            String key = slotStart.format(HOUR_FORMATTER);
            long count = incidentRepository.countByCreatedAtBetween(start, end);
            incidentsByHour.put(key, count);
        }
        return incidentsByHour;
    }

    private Instant startOfDay() {
        return LocalDate.now(ZONE).atStartOfDay(ZONE).toInstant();
    }

    private Instant startOfWeek() {
        return LocalDate.now(ZONE)
                .with(DayOfWeek.MONDAY)
                .atStartOfDay(ZONE)
                .toInstant();
    }

    private Instant startOfMonth() {
        return LocalDate.now(ZONE)
                .withDayOfMonth(1)
                .atStartOfDay(ZONE)
                .toInstant();
    }

    private Instant startOfYear() {
        return LocalDate.now(ZONE)
                .withDayOfYear(1)
                .atStartOfDay(ZONE)
                .toInstant();
    }
}
