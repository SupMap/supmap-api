// AuthController.java
package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface AuthController {

    @PostMapping("/auth/register")
    CompletableFuture<TokenResponseDto> registerUser(@RequestBody RegisterDto user);

    @PostMapping("/auth/login")
    TokenResponseDto loginUser(@RequestBody LoginDto user);
}
