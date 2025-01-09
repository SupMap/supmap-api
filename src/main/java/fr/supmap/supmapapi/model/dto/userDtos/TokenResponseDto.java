package fr.supmap.supmapapi.model.dto.userDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The Login Responsse DTO.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto implements Serializable {

    private String token;

}