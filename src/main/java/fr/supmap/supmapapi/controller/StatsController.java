package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.StatsDto;
import org.springframework.web.bind.annotation.GetMapping;

public interface StatsController {

    @GetMapping("/stats")
    StatsDto getStats();
}
