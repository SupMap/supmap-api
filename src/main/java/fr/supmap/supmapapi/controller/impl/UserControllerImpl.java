// UserControllerImpl.java
package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.UserController;
import fr.supmap.supmapapi.model.dto.userDtos.UserMinimalInfoDto;
import fr.supmap.supmapapi.model.dto.userDtos.UserUpdateDto;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.TokenManager;
import fr.supmap.supmapapi.services.exceptions.NotAuthorizeException;
import fr.supmap.supmapapi.services.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static fr.supmap.supmapapi.services.PasswordManager.hashPassword;

@RestController
@Tag(name = "Gestion du User")
public class UserControllerImpl implements UserController {

    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final Logger log = LoggerFactory.getLogger(UserControllerImpl.class);

    public UserControllerImpl(UserRepository userRepository, TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
    }

    @Override
    @Operation(description = "Permet de lister tous les utilisateurs (désactivé)", summary = "Get All Users (disabled)")
    public List<User> getAll() {
        throw new NotAuthorizeException("Ressource désactivée");
    }

    @Override
    @Operation(description = "Permet de récupérer les informations d'un utilisateur spécifique", summary = "Get User")
    public UserMinimalInfoDto getUser(Integer userId) {
        log.info("GET /users/{}", userId);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        return UserMinimalInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .secondName(user.getSecondName())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Operation(description = "Permet de récupérer les informations de l'utilisateur connecté", summary = "Get User Info")
    public UserMinimalInfoDto getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = Integer.parseInt(authentication.getName());

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        log.info("GET /users/info - User: {}", user.getUsername());

        return UserMinimalInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .secondName(user.getSecondName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    @Operation(description = "Permet de modifier les infos d'un user", summary = "Update user")
    public UserMinimalInfoDto updateUser(UserUpdateDto userUpdateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = Integer.parseInt(authentication.getName());

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        userUpdateDto.getUsername().ifPresent(user::setUsername);
        userUpdateDto.getPassword().ifPresent(password -> user.setPasswordHash(hashPassword(password)));
        userUpdateDto.getName().ifPresent(user::setName);
        userUpdateDto.getSecondName().ifPresent(user::setSecondName);
        userUpdateDto.getEmail().ifPresent(user::setEmail);

        User updatedUser = userRepository.save(user);

        log.info("PATCH /users - User: {}", updatedUser.getUsername());

        return UserMinimalInfoDto.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .name(updatedUser.getName())
                .secondName(updatedUser.getSecondName())
                .email(updatedUser.getEmail())
                .build();
    }
}