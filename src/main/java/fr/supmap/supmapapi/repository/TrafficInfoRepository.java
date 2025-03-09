package fr.supmap.supmapapi.repository;

import fr.supmap.supmapapi.model.entity.table.TrafficInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrafficInfoRepository extends JpaRepository<TrafficInfo, Integer> {
}