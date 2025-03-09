package fr.supmap.supmapapi.model.entity.table;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "total_duration")
    private Double totalDuration;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "start_location", columnDefinition = "geography not null")
    private Object startLocation;

    @Column(name = "end_location", columnDefinition = "geography not null")
    private Object endLocation;

    @Column(name = "route_geometry", columnDefinition = "geography not null")
    private Object routeGeometry;

}