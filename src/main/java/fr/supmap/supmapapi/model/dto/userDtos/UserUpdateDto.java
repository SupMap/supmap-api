package fr.supmap.supmapapi.model.dto.userDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

/**
 * The User Update DTO.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto implements Serializable {

    private Optional<String> username;
    private Optional<String> password;
    private Optional<String> name;
    private Optional<String> secondName;
    private Optional<String> email;

    public Optional<String> getUsername() {
        return username == null ? Optional.empty() : username;
    }

    public Optional<String> getPassword() {
        return password == null ? Optional.empty() : password;
    }

    public Optional<String> getName() {
        return name == null ? Optional.empty() : name;
    }

    public Optional<String> getSecondName() {
        return secondName == null ? Optional.empty() : secondName;
    }

    public Optional<String> getEmail() {
        return email == null ? Optional.empty() : email;
    }
}