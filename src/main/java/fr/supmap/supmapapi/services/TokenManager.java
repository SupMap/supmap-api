package fr.supmap.supmapapi.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.supmap.supmapapi.model.entity.table.User;
import fr.supmap.supmapapi.repository.UserRepository;
import fr.supmap.supmapapi.services.exceptions.NotAuthorizeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
public class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);
    private static UserRepository userRepository;

    private static String secretKey;

    /**
     * Instantiates a new Token manager.
     *
     * @param userRepository the user repository
     * @param secretKey      the secret key
     */
    public TokenManager(UserRepository userRepository, @Value("${token.secret-key}") String secretKey) {
        TokenManager.userRepository = userRepository;
        TokenManager.secretKey = secretKey;
    }

    /**
     * Gets user (userId) from token.
     *
     * @param token the token
     * @return the user from token
     */
    public static Integer getUserFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            return Integer.parseInt(jwt.getSubject());

        } catch (JWTVerificationException exception) {
            log.error("Error while verifying token");
            throw new NotAuthorizeException("Invalid token");
        }
    }

    /**
     * Gets user from token.
     *
     * @return the user
     */
    public static User getUser() {
        return userRepository.findById(Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName())).orElseThrow(() -> new ResponseStatusException(HttpStatus.PRECONDITION_FAILED));
    }

    /**
     * Create token string.
     *
     * @param userId the user id
     * @return the string
     */
    public String createToken(Integer userId) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        return "Bearer " + JWT.create()
                .withSubject(userId.toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + 36000 * 1000))
                .withIssuer("auth0")
                .sign(algorithm);
    }
}
