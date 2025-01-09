package fr.supmap.supmapapi.model.dto.userDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The Login DTO.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto implements Serializable {

    private String loginUser;
    private String password;

}