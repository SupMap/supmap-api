// AuthController.java
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

public interface AuthController {

    @PostMapping("/auth/register")
    CompletableFuture<TokenResponseDto> registerUser(@RequestBody RegisterDto user);

    @PostMapping("/auth/login")
    TokenResponseDto loginUser(@RequestBody LoginDto user);

    @PostMapping("/auth/google/mobile")
    ResponseEntity<?> googleMobileLogin(@RequestBody IdTokenDto body) throws GeneralSecurityException, IOException;
}
