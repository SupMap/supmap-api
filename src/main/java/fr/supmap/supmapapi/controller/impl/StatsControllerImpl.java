package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.StatsController;
import fr.supmap.supmapapi.model.dto.StatsDto;
import fr.supmap.supmapapi.repository.IncidentRepository;
import fr.supmap.supmapapi.repository.RouteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@Tag(name = "Gestion des statistiques")
public class StatsControllerImpl implements StatsController {

    private final RouteRepository routeRepository;
    private final IncidentRepository incidentRepository;

    public StatsControllerImpl(RouteRepository routeRepository, IncidentRepository incidentRepository) {
        this.routeRepository = routeRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    @Operation(description = "Permet de récupérer les statistiques", summary = "Get Stats")
    public StatsDto getStats() {
        Instant now = Instant.now();
        StatsDto stats = new StatsDto();

        stats.setOngoingTrips(routeRepository.countByTotalDurationIsNull());
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
