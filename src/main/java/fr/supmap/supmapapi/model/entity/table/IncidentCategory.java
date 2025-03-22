package fr.supmap.supmapapi.model.entity.table;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "incident_categories")
public class IncidentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "incident_categories_id_gen")
    @SequenceGenerator(name = "incident_categories_id_gen", sequenceName = "incident_categories_category_id_seq", allocationSize = 1)
    @Column(name = "category_id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

}