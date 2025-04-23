package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    List<Incident> findByConfirmedByUserId(Integer userId);
    long countByExpirationDateAfter(Instant now);
    long countByCreatedAtBetween(Instant start, Instant end);
}