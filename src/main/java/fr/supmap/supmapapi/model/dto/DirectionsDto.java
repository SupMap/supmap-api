package fr.supmap.supmapapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectionsDto {
    private String fastest;
    private String noToll;
    private String economical;
}
