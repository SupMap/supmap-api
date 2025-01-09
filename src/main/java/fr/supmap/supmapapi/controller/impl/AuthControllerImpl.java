package fr.supmap.supmapapi.controller.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import fr.supmap.supmapapi.controller.AuthController;
import fr.supmap.supmapapi.model.dto.userDtos.LoginDto;
import fr.supmap.supmapapi.model.dto.userDtos.RegisterDto;
import fr.supmap.supmapapi.model.dto.userDtos.TokenResponseDto;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.PasswordManager;
import fr.supmap.supmapapi.services.TokenManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The Authentication controller.
 */
@RestController
@Tag(name = "Gestion de l'authentification")
public class AuthControllerImpl implements AuthController {

    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final Logger log = LoggerFactory.getLogger(AuthControllerImpl.class);
    @Value("${google.oauth.id.client.key}")
    private String clientId;
    @Value("${google.oauth.secret.client.key}")
    private String clientSecret;
    @Value("${google.oauth.redirect.uri}")
    private String redirectUri;
    private GoogleAuthorizationCodeFlow flow;

    public AuthControllerImpl(UserRepository userRepository, TokenManager tokenManager) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
    }

    public static String generateRandomPassword() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Integer LENGTH = 15;
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }

    @PostConstruct
    private void init() {
        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                Arrays.asList(
                        "https://www.googleapis.com/auth/contacts.readonly",
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/userinfo.email"
                ))
                .setAccessType("offline")
                .build();
    }

    @Override
    @Operation(description = "Permet de login un utilisateur", summary = "Login")
    public TokenResponseDto login(LoginDto user) {
        Optional<User> foundUser = userRepository.findByUsernameOrEmail(user.getLoginUser(), user.getLoginUser());

        if (foundUser.isPresent() && PasswordManager.verifyPassword(user.getPassword(), foundUser.get().getPasswordHash())) {
            String token = tokenManager.createToken(foundUser.get().getId());


            TokenResponseDto loginResponse = TokenResponseDto.builder()
                    .token(token)
                    .build();

            this.log.info("POST /login User: {}", foundUser.get().getUsername());
            return loginResponse;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }


    @Override
    @Operation(description = "Permet de login un utilisateur via Oauth2", summary = "Login Oauth2")
    public void loginOauth2(HttpServletRequest request, HttpServletResponse response) {
        this.log.info("POST /login/2oauth");

        String code = request.getParameter("code");
        try {
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            Credential credential = flow.createAndStoreCredential(tokenResponse, null);

            Map<String, Object> userInformation = getUserInfo(credential);
            if (userInformation != null) {
                log.info("Informations de l'utilisateur récupérées avec succès.");

                TokenResponseDto tokenResponseDto = this.oauth2Gestion(userInformation);

                String token = tokenResponseDto.getToken().replace("Bearer ", "");

                // URL de redirection vers votre front-end avec le token en paramètre
                String targetUrl = "http://localhost:3000/handle-oauth2?token=" + token;

                response.sendRedirect(targetUrl);

            } else {
                log.error("Impossible de récupérer les informations de l'utilisateur.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code");
        }
    }

    @Override
    public String loginOauth2Url() {

        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();
    }

    @Override
    @Operation(description = "Permet de register un utilisateur", summary = "Register")
    public CompletableFuture<TokenResponseDto> register(RegisterDto user) {
        return CompletableFuture.supplyAsync(() -> {
            this.log.info("POST /register");

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

            try {
                userRepository.save(newUser);
                Integer userId = newUser.getId();
                String token = tokenManager.createToken(userId);


                TokenResponseDto loginResponse = TokenResponseDto.builder()
                        .token(token)
                        .build();
                return loginResponse;
            } catch (Exception e) {
                this.log.error("Failed to register new user: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to register user. Please check the details and try again");
            }
        });
    }


    private Map<String, Object> getUserInfo(Credential credential) throws IOException {

        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v1/userinfo?alt=json");
        HttpRequest request = requestFactory.buildGetRequest(url);

        HttpHeaders headers = new HttpHeaders();
        headers.setAuthorization("Bearer " + credential.getAccessToken());
        request.setHeaders(headers);

        HttpResponse response = request.execute();
        String jsonResponse = response.parseAsString();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> userInfo = mapper.readValue(jsonResponse, Map.class);

        return userInfo;
    }

    private TokenResponseDto oauth2Gestion(Map<String, Object> userInformation) {

        BigInteger oauth2Id = new BigInteger(userInformation.get("id").toString());

        Optional<User> userFound = userRepository.findByOauth2Id(oauth2Id);

        User user = new User();

        if (!userFound.isPresent()) {
            user.setOauth2Id(oauth2Id);
            user.setEmail(userInformation.get("email").toString());
            user.setUsername(userInformation.get("name").toString());
            user.setName(userInformation.get("given_name").toString());

            if (userInformation.containsKey("family_name") && userInformation.get("family_name") != null) {
                user.setSecondName(userInformation.get("family_name").toString());
            } else {
                user.setSecondName("");
            }
            user.setCreationDate(new Date().toInstant());

            String password = generateRandomPassword();
            user.setPasswordHash(PasswordManager.hashPassword(password));

            user = userRepository.save(user);

        } else {
            user = userFound.get();
        }

        String token = tokenManager.createToken(user.getId());

        TokenResponseDto loginResponse = TokenResponseDto.builder()
                .token(token)
                .build();

        this.log.info("POST /login User: {}", user.getUsername());
        return loginResponse;
    }
}
