package fr.supmap.supmapapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsDto {private Long ongoingTrips;
    private Long tripsToday;
    private Long tripsThisWeek;
    private Long tripsThisMonth;
    private Long tripsThisYear;

    private Long ongoingIncidents;
    private Long incidentsToday;
    private Long incidentsThisWeek;
    private Long incidentsThisMonth;
    private Long incidentsThisYear;

    private Double averageTripDuration;
    private Double averageTripDistance;
}
