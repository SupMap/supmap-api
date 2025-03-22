package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.entity.table.TrafficInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface TrafficInfoController {

    @PostMapping("/traffic")
    TrafficInfo createTrafficInfo(@RequestBody TrafficInfo trafficInfo);

    @GetMapping("/traffic")
    List<TrafficInfo> getAllTrafficInfo();
}
