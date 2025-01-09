package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.CompletableFuture;

/**
 * The interface Authentication controller.
 *
 * @author Mathéo RIO (matheo.rio@supinfo.com)
 */
public interface AuthController {


    /**
     * Login the user
     *
     * @param user the user
     * @return the user
     */
    @PostMapping("/auth/login")
    TokenResponseDto login(@RequestBody LoginDto user);

    /**
     * Oauth2 login
     *
     * @param request  the request
     * @param response the response
     * @return the token
     */
    @GetMapping("/auth/login/oauth2/verification")
    void loginOauth2(HttpServletRequest request, HttpServletResponse response);

    /**
     * Get oauth 2 url.
     *
     * @return the string
     */
    @GetMapping("/auth/login/oauth2")
    String loginOauth2Url();

    /**
     * Create a new user.
     *
     * @param user the user
     * @return the token response dto
     */
    @PostMapping("/auth/register")
    CompletableFuture<TokenResponseDto> register(@RequestBody RegisterDto user);
}
