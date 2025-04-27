// AuthControllerImpl.java
package fr.supmap.supmapapi.controller.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import fr.supmap.supmapapi.controller.AuthController;
import fr.supmap.supmapapi.model.dto.IdTokenDto;
import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.RoleRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.PasswordManager;
import fr.supmap.supmapapi.services.TokenManager;
import fr.supmap.supmapapi.utils.PasswordUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
            if (userRepository.count() == 0) {
                u.setRole(roleRepository.findByName("Administrateur"));
            } else {
                u.setRole(roleRepository.findByName("Utilisateur"));
            }
            u.setContribution(0);
            userRepository.save(u);

            String token = tokenManager.createToken(u.getId());
            return TokenResponseDto.builder().token(token).build();
        });
    }

    @Override
    @Operation(summary = "Login avec Google")
    public ResponseEntity<?> googleMobileLogin(IdTokenDto body) throws GeneralSecurityException, IOException {
        // 1) Vérifier l’ID Token
        var verifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(List.of("TON_WEB_CLIENT_ID.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(body.idToken());
        if (idToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2) Récupérer les infos
        var payload = idToken.getPayload();
        String sub   = payload.getSubject();
        String email = payload.getEmail();
        String given = (String) payload.get("given_name");
        String family= (String) payload.get("family_name");

        // 3) Find or create user (même logique que dans OAuth2LoginSuccessHandler)
        User user = userRepository.findByOauth2Id(sub)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setUsername(email);
                    u.setName(given);
                    u.setSecondName(family);
                    u.setCreationDate(Instant.now());
                    u.setRole(roleRepository.findByName("Utilisateur"));
                    String pwd = PasswordUtils.generateRandomPassword(12);
                    u.setPasswordHash(PasswordManager.hashPassword(pwd));
                    u.setContribution(0);
                    u.setOauth2Id(sub);
                    return userRepository.save(u);
                });

        if (user.getOauth2Id() == null) {
            user.setOauth2Id(sub);
            userRepository.save(user);
        }

        // 4) Générer ton JWT
        String jwt = tokenManager.createToken(user.getId()).replace("Bearer ", "");

        // 5) Retourner le token dans le corps
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}

