package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.IncidentController;
import fr.supmap.supmapapi.model.entity.table.Incident;
import fr.supmap.supmapapi.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IncidentControllerImpl implements IncidentController {

    @Autowired
    private IncidentRepository incidentRepository;

    @Override
    public Incident createIncident(Incident incident) {
        return incidentRepository.save(incident);
    }

    @Override
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }
}
