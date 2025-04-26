// AuthControllerImpl.java
package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.AuthController;
import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.RoleRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.PasswordManager;
import fr.supmap.supmapapi.services.TokenManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@Tag(name = "Gestion de l'authentification")
public class AuthControllerImpl implements AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenManager tokenManager;
    private final Logger log = LoggerFactory.getLogger(AuthControllerImpl.class);

    public AuthControllerImpl(UserRepository userRepository,
                              RoleRepository roleRepository,
                              TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenManager   = tokenManager;
    }

    @Override
    @Operation(summary = "Login classique")
    public TokenResponseDto loginUser(LoginDto user) {
        Optional<User> found = userRepository.findByUsernameOrEmail(user.getLoginUser(), user.getLoginUser());
        if (found.isPresent() && PasswordManager.verifyPassword(user.getPassword(), found.get().getPasswordHash())) {
            String token = tokenManager.createToken(found.get().getId());
            log.info("POST /api/auth/login User: {}", found.get().getUsername());
            return TokenResponseDto.builder().token(token).build();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @Override
    @Operation(summary = "Register")
    public CompletableFuture<TokenResponseDto> registerUser(RegisterDto user) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("POST /api/auth/register");
            if (userRepository.existsByUsername(user.getUsername())
                    || userRepository.existsByEmail(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "An account already exists with this username or email");
            }
            User u = new User();
            u.setUsername(user.getUsername());
            u.setName(user.getName());
            u.setSecondName(user.getSecondName());
            u.setEmail(user.getEmail());
            u.setPasswordHash(PasswordManager.hashPassword(user.getPassword()));
            u.setCreationDate(new Date().toInstant());
            u.setRole(roleRepository.findByName("Utilisateur"));
            u.setContribution(0);
            userRepository.save(u);

            String token = tokenManager.createToken(u.getId());
            return TokenResponseDto.builder().token(token).build();
        });
    }
}
