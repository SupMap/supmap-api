package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.IdTokenDto;
import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.CompletableFuture;

/**
 * The interface Auth controller.
 */
public interface AuthController {

    /**
     * Register user completable future.
     *
     * @param user the user
     * @return the completable future
     */
    @PostMapping("/auth/register")
    CompletableFuture<TokenResponseDto> registerUser(@RequestBody RegisterDto user);

    /**
     * Login user token response dto.
     *
     * @param user the user
     * @return the token response dto
     */
    @PostMapping("/auth/login")
    TokenResponseDto loginUser(@RequestBody LoginDto user);

    /**
     * Google mobile login response entity.
     *
     * @param body the body
     * @return the response entity
     * @throws GeneralSecurityException the general security exception
     * @throws IOException              the io exception
     */
    @PostMapping("/auth/google/mobile")
    TokenResponseDto googleMobileLogin(@RequestBody IdTokenDto body);
}
