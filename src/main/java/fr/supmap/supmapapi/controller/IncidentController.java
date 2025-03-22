package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.IncidentDto;
import fr.supmap.supmapapi.model.entity.table.Incident;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface IncidentController {

    @PostMapping("/incidents")
    Incident createIncident(@RequestBody IncidentDto incident);

    @GetMapping("/incidents")
    List<IncidentDto> getAllIncidents();
}
