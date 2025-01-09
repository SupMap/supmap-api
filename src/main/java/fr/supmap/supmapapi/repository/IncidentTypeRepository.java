package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentTypeRepository extends JpaRepository<IncidentType, Integer> {
}