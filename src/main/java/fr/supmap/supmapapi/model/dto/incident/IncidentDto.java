package fr.supmap.supmapapi.model.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {
    private Integer typeId;
    private Double latitude;
    private Double longitude;
}
