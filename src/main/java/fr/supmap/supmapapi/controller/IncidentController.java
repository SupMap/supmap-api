package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.incident.IncidentDto;
import fr.supmap.supmapapi.model.dto.incident.IncidentResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The interface Incident controller.
 */
public interface IncidentController {

    /**
     * Create incident response entity.
     *
     * @param incident the incident
     * @return the response entity
     */
    @PostMapping("/incidents")
    ResponseEntity<String> createIncident(@RequestBody IncidentDto incident);

    /**
     * Gets all incidents.
     *
     * @return the all incidents
     */
    @GetMapping("/incidents")
    List<IncidentResponseDto> getAllIncidents();

    /**
     * Gets user incidents.
     *
     * @return the user incidents
     */
    @GetMapping("/user/incidents")
    List<IncidentDto> getUserIncidents();

    /**
     * Rate incident response entity.
     *
     * @param id       the id
     * @param positive the positive
     * @return the response entity
     */
    @GetMapping("/incident/{id}/rate")
    ResponseEntity<String> rateIncident(@PathVariable Integer id, @RequestParam boolean positive);
}
