package fr.supmap.supmapapi.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.supmap.supmapapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenManager {

    private final Logger log = LoggerFactory.getLogger(TokenManager.class);
    private final Algorithm algorithm;

    public TokenManager(UserRepository userRepository, @Value("${token.secret-key}") String secretKey) {
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    public String createToken(Integer userId) {
        return "Bearer " + JWT.create()
                .withSubject(userId.toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + 36000 * 1000))
                .withIssuer("auth0")
                .sign(algorithm);
    }

    public Integer verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return Integer.parseInt(jwt.getSubject());
        } catch (JWTVerificationException exception) {
            log.error("Invalid token: {}", exception.getMessage());
            throw new IllegalArgumentException("Invalid token");
        }
    }
}
