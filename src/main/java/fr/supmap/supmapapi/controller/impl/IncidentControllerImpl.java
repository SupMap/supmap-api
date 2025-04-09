package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.IncidentController;
import fr.supmap.supmapapi.model.dto.IncidentDto;
import fr.supmap.supmapapi.model.entity.table.Incident;
import fr.supmap.supmapapi.model.entity.table.IncidentType;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.IncidentRepository;
import fr.supmap.supmapapi.repository.IncidentTypeRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.utils.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
public class IncidentControllerImpl implements IncidentController {

    private final Logger log = LoggerFactory.getLogger(IncidentControllerImpl.class);
    private final IncidentTypeRepository incidentTypeRepository;
    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;

    public IncidentControllerImpl(IncidentTypeRepository incidentTypeRepository, UserRepository userRepository, IncidentRepository incidentRepository) {
        this.incidentTypeRepository = incidentTypeRepository;
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
    }

    private User GetUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        int userId = Integer.parseInt(authentication.getName());

        return userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    @Override
    public Incident createIncident(IncidentDto incidentDto) {
        log.info("POST /incidents incidentDto: {}", incidentDto);

        IncidentType incidentType = this.incidentTypeRepository.findById(incidentDto.getTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Type d'incident non trouvé"));

        Incident newIncident = new Incident();
        newIncident.setType(incidentType);
        newIncident.setLocation(GeoUtils.createPoint(incidentDto.getLatitude(), incidentDto.getLongitude()));
        newIncident.setCreatedAt(Instant.now());

        User user = GetUserAuthenticated();
        newIncident.setConfirmedByUser(user);

        try {
            return incidentRepository.save(newIncident);
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'incident : {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création de l'incident", e);
        }
    }

    @Override
    public List<IncidentDto> getAllIncidents() {
        log.info("GET /incidents");
        List<Incident> incidents = incidentRepository.findAll();
        List<IncidentDto> incidentDtoList = new ArrayList<>();
        for (Incident incident : incidents) {
            IncidentDto dto = IncidentDto.builder()
                    .typeId(incident.getType().getId())
                    .typeName(incident.getType().getName())
                    .latitude(incident.getLocation().getY())
                    .longitude(incident.getLocation().getX())
                    .build();
            incidentDtoList.add(dto);
        }
        return incidentDtoList;
    }

    @Override
    public List<IncidentDto> getUserIncidents() {
        User user = GetUserAuthenticated();
        log.info("GET /user/incidents for user: {}", user.getUsername());
        List<Incident> incidents = incidentRepository.findByConfirmedByUserId(user.getId());
        List<IncidentDto> incidentDtoList = new ArrayList<>();
        for (Incident incident : incidents) {
            IncidentDto dto = IncidentDto.builder()
                    .typeId(incident.getType().getId())
                    .typeName(incident.getType().getName())
                    .latitude(incident.getLocation().getY())
                    .longitude(incident.getLocation().getX())
                    .build();
            incidentDtoList.add(dto);
        }
        return incidentDtoList;
    }
}