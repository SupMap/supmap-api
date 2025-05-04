package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.StatsDto;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The interface Stats controller.
 */
public interface StatsController {

    /**
     * Gets stats.
     *
     * @return the stats
     */
    @GetMapping("/stats")
    StatsDto getStats();
}
