package fr.supmap.supmapapi.model.dto.userDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The User DTO without password.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMinimalInfoDto implements Serializable {

    private Integer id;
    private String username;
    private String name;
    private String secondName;
    private String email;

}