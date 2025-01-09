package fr.supmap.supmapapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Oauth2 DTO.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Oauth2Dto {

    private String code;
}
