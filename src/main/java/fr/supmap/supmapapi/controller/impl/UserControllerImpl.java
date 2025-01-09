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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static fr.supmap.supmapapi.services.PasswordManager.hashPassword;

/**
 * The User controller.
 */
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
        if (true) {
            //Ressource désactivé pour des raisons de sécurité
            throw new NotAuthorizeException("Ressource désactivée");
        }
        log.info("GET /users");
        return this.userRepository.findAll();
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

        try {
            User user = TokenManager.getUser();
            log.info("GET /users/info - User: {}", user.getUsername());

            return UserMinimalInfoDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .secondName(user.getSecondName())
                    .email(user.getEmail())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Erreur de récupération des données de l'utilisateur");
        }
    }

    @Transactional
    @Operation(description = "Permet de modifier les infos d'un user", summary = "Update user")
    public UserMinimalInfoDto updateUser(UserUpdateDto userUpdateDto) {

        try {
            log.info("PATCH /users - User: {}", TokenManager.getUser().getUsername());
            User user = TokenManager.getUser();

            userUpdateDto.getUsername().ifPresent(user::setUsername);
            userUpdateDto.getPassword().ifPresent(password -> user.setPasswordHash(hashPassword(password)));
            userUpdateDto.getName().ifPresent(user::setName);
            userUpdateDto.getSecondName().ifPresent(user::setSecondName);
            userUpdateDto.getEmail().ifPresent(user::setEmail);

            User updatedUser = userRepository.save(user);

            return UserMinimalInfoDto.builder()
                    .id(updatedUser.getId())
                    .username(updatedUser.getUsername())
                    .name(updatedUser.getName())
                    .secondName(updatedUser.getSecondName())
                    .email(updatedUser.getEmail())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour des informations de l'utilisateur");
        }

    }
}
