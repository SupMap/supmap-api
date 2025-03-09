package fr.supmap.supmapapi.model.entity.table;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Entity
@Table(name = "traffic_info")
public class TrafficInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "traffic_info_id_gen")
    @SequenceGenerator(name = "traffic_info_id_gen", sequenceName = "traffic_info_traffic_id_seq", allocationSize = 1)
    @Column(name = "traffic_id", nullable = false)
    private Integer id;

    @Column(name = "congestion_level")
    private Integer congestionLevel;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "location", columnDefinition = "geography not null")
    private Point location;

}