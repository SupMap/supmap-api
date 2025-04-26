package fr.supmap.supmapapi.model.entity.table;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "routes_id_gen")
    @SequenceGenerator(name = "routes_id_gen", sequenceName = "routes_route_id_seq", allocationSize = 1)
    @Column(name = "route_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private fr.supmap.supmapapi.model.entity.table.User user;

    @Column(name = "total_duration")
    private Double totalDuration;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Size(max = 50)
    @Column(name = "custom_model", length = 50)
    private String customModel;

    @Size(max = 15)
    @Column(name = "mode", length = 50)
    private String mode;


    @Column(name = "start_location", columnDefinition = "geography(Point,4326) not null")
    private Point startLocation;

    @Column(name = "end_location", columnDefinition = "geography(Point,4326) not null")
    private Point endLocation;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "route_geometry", columnDefinition = "geography(LineString,4326) not null")
    private LineString routeGeometry;

    @Column(name = "active")
    private Boolean active;
}