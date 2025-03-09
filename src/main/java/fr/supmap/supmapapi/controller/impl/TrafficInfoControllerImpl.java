package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.TrafficInfoController;
import fr.supmap.supmapapi.model.entity.table.TrafficInfo;
import fr.supmap.supmapapi.repository.TrafficInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TrafficInfoControllerImpl implements TrafficInfoController {

    @Autowired
    private TrafficInfoRepository trafficInfoRepository;

    @Override
    public TrafficInfo createTrafficInfo(TrafficInfo trafficInfo) {
        return trafficInfoRepository.save(trafficInfo);
    }

    @Override
    public List<TrafficInfo> getAllTrafficInfo() {
        return trafficInfoRepository.findAll();
    }
}
