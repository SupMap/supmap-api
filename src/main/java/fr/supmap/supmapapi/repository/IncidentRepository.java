package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {
}