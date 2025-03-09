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
import org.locationtech.jts.geom.LineString;

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
    private User user;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "total_duration")
    private Double totalDuration;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    // Utiliser le type Point pour la localisation de départ et d'arrivée
    @Column(name = "start_location", columnDefinition = "geography(Point,4326) not null")
    private Point startLocation;

    @Column(name = "end_location", columnDefinition = "geography(Point,4326) not null")
    private Point endLocation;

    // Utiliser le type LineString pour la géométrie du trajet
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "route_geometry", columnDefinition = "geography(LineString,4326) not null")
    private LineString routeGeometry;
}
