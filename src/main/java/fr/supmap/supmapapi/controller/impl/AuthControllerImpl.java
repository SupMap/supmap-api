// AuthControllerImpl.java
package fr.supmap.supmapapi.controller.impl;

import fr.supmap.supmapapi.controller.AuthController;
import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.PasswordManager;
import fr.supmap.supmapapi.services.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
public class AuthControllerImpl implements AuthController {

    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final Logger log = LoggerFactory.getLogger(AuthControllerImpl.class);

    public AuthControllerImpl(UserRepository userRepository, TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
    }

    @Override
    public String getOAuth2LoginUrl() {
        return "/oauth2/authorization/google";
    }

    @Override
    public String handleOAuth2Success() {
        return "OAuth2 login successful!";
    }

    @Override
    public TokenResponseDto loginUser(LoginDto user) {
        Optional<User> foundUser = userRepository.findByUsernameOrEmail(user.getLoginUser(), user.getLoginUser());

        if (foundUser.isPresent() && PasswordManager.verifyPassword(user.getPassword(), foundUser.get().getPasswordHash())) {
            String token = tokenManager.createToken(foundUser.get().getId());

            TokenResponseDto loginResponse = TokenResponseDto.builder()
                    .token(token)
                    .build();

            this.log.info("POST /api/auth/login User: {}", foundUser.get().getUsername());
            return loginResponse;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @Override
    public CompletableFuture<TokenResponseDto> registerUser(RegisterDto user) {
        return CompletableFuture.supplyAsync(() -> {
            this.log.info("POST /api/auth/register");

            if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "An account already exists with this username or email");
            }
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setName(user.getName());
            newUser.setSecondName(user.getSecondName());
            newUser.setEmail(user.getEmail());
            newUser.setPasswordHash(PasswordManager.hashPassword(user.getPassword()));
            newUser.setCreationDate(new Date().toInstant());

            userRepository.save(newUser);
            String token = tokenManager.createToken(newUser.getId());

            return TokenResponseDto.builder().token(token).build();
        });
    }
}
