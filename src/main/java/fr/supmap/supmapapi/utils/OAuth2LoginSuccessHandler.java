package fr.supmap.supmapapi.utils;

import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.RoleRepository;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.PasswordManager;
import fr.supmap.supmapapi.services.TokenManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TokenManager tokenManager;

    public OAuth2LoginSuccessHandler(UserRepository userRepo,
                                     RoleRepository roleRepo,
                                     TokenManager tokenManager) {
        this.userRepo      = userRepo;
        this.roleRepo      = roleRepo;
        this.tokenManager  = tokenManager;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String oauth2Id = oauthUser.getAttribute("sub");
        String email    = oauthUser.getAttribute("email");

        User user = userRepo.findByOauth2Id(oauth2Id)
                .or(() -> userRepo.findByEmail(email))
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setUsername(email);
                    u.setName(oauthUser.getAttribute("given_name"));
                    u.setSecondName(oauthUser.getAttribute("family_name"));
                    u.setCreationDate(Instant.now());
                    u.setRole(roleRepo.findByName("Utilisateur"));
                    String randomPwd = PasswordUtils.generateRandomPassword(12);
                    u.setPasswordHash(PasswordManager.hashPassword(randomPwd));
                    u.setContribution(0);
                    u.setOauth2Id(oauth2Id);
                    return userRepo.save(u);
                });

        if (user.getOauth2Id() == null) {
            user.setOauth2Id(oauth2Id);
            userRepo.save(user);
        }

        String token = tokenManager.createToken(user.getId());
        token = token.replace("Bearer ", "");
        response.sendRedirect("http://localhost:5173/#token=" + token);
    }
}
