package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Integer> {
    Route findByUserId(int userId);
}