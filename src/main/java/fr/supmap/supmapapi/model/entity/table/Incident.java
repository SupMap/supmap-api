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
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "incidents_id_gen")
    @SequenceGenerator(name = "incidents_id_gen", sequenceName = "incidents_incident_id_seq", allocationSize = 1)
    @Column(name = "incident_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private IncidentType type;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "\"timestamp\"", nullable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by_user_id")
    private fr.supmap.supmapapi.model.entity.table.User confirmedByUser;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "location", columnDefinition = "geography(Point,4326) not null")
    private Point location;
}