package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public interface AuthController {

    @GetMapping("/auth/login/oauth2")
    String getOAuth2LoginUrl();

    @GetMapping("/auth/login/oauth2/success")
    String handleOAuth2Success();

    @PostMapping("/auth/register")
    CompletableFuture<TokenResponseDto> registerUser(@RequestBody RegisterDto user);

    @PostMapping("/auth/login")
    TokenResponseDto loginUser(@RequestBody LoginDto user);
    }