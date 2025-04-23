package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface RouteRepository extends JpaRepository<Route, Integer> {
    Route findByUserId(int userId);
    long countByTotalDurationIsNull();
    long countByCalculatedAtBetween(Instant start, Instant end);

    @Query("SELECT COALESCE(AVG(r.totalDuration),0) FROM Route r WHERE r.totalDuration IS NOT NULL")
    Double averageTotalDuration();

    @Query("SELECT COALESCE(AVG(r.totalDistance),0) FROM Route r WHERE r.totalDistance IS NOT NULL")
    Double averageTotalDistance();
}