package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.IncidentController;
import fr.supmap.supmapapi.model.dto.IncidentDto;
import fr.supmap.supmapapi.model.entity.table.Incident;
import fr.supmap.supmapapi.model.entity.table.IncidentType;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.IncidentRepository;
import fr.supmap.supmapapi.repository.IncidentTypeRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class IncidentControllerImpl implements IncidentController {
    private final Logger log = LoggerFactory.getLogger(UserControllerImpl.class);
    private final IncidentTypeRepository incidentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    public IncidentControllerImpl(IncidentTypeRepository incidentTypeRepository) {
        this.incidentTypeRepository = incidentTypeRepository;
    }

    @Override
    public Incident createIncident(IncidentDto incident) {
        log.info("POST /incidents incident : {}", incident);

        IncidentType incidentType = this.incidentTypeRepository.findById(incident.getTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Type d'incident non trouvé"));

        Incident newIncident = new Incident();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = Integer.parseInt(authentication.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));


        newIncident.setCreatedAt(Instant.now());
        newIncident.setLatitude(incident.getLatitude());
        newIncident.setLongitude(incident.getLongitude());
        newIncident.setIncidentType(incidentType);
        newIncident.setConfirmedByUser(user);


        try {
            return incidentRepository.save(newIncident);
        } catch (Exception e) {
            log.error("Error while creating incident : {}", e.getMessage());
            return null;
        }
    }


    @Override
    public List<IncidentDto> getAllIncidents() {
        log.info("GET /incidents");
        List<Incident> incidents = this.incidentRepository.findAll();


        List<IncidentDto> incidentDtoList = new ArrayList<>();
        for (Incident incident : incidents) {
            IncidentDto incidentDto = new IncidentDto();
            incidentDto.setTypeId(incident.getIncidentType().getId());
            incidentDto.setLatitude(incident.getLatitude());
            incidentDto.setLongitude(incident.getLongitude());
            incidentDtoList.add(incidentDto);
        }
        return incidentDtoList;
    }
}
