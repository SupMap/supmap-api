package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.IncidentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentCategoryRepository extends JpaRepository<IncidentCategory, Integer> {
}