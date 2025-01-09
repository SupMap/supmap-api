package fr.supmap.supmapapi.model.entity.table;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "incident_types")
public class IncidentType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "incident_types_id_gen")
    @SequenceGenerator(name = "incident_types_id_gen", sequenceName = "incident_types_type_id_seq", allocationSize = 1)
    @Column(name = "type_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private IncidentCategory category;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

}