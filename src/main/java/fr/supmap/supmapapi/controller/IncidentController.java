package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.incident.IncidentDto;
import fr.supmap.supmapapi.model.dto.incident.IncidentResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface IncidentController {

    @PostMapping("/incidents")
    ResponseEntity<String> createIncident(@RequestBody IncidentDto incident);

    @GetMapping("/incidents")
    List<IncidentResponseDto> getAllIncidents();

    @GetMapping("/user/incidents")
    List<IncidentDto> getUserIncidents();

    @GetMapping("/incident/{id}/rate")
    ResponseEntity<String> rateIncident(@PathVariable Integer id, @RequestParam boolean positive);
}
