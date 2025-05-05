package fr.supmap.supmapapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDto {
    private String Route;
    private Double totalDuration;
    private Double totalDistance;
    private String customModel;
    private String mode;
    private String startLocation;
    private String endLocation;
}
