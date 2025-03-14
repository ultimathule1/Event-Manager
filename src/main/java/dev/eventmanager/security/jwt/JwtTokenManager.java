package dev.eventmanager.security.jwt;

import dev.eventmanager.users.db.UserRepository;
import dev.eventmanager.users.domain.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.NoSuchElementException;

@Component
public class JwtTokenManager {

    private static final Logger log = LogManager.getLogger(JwtTokenManager.class);
    private final SecretKey key;
    private final long expirationTime;
    private final UserRepository userRepository;


    public JwtTokenManager(
            @Value("${jwt.secret-key}") String key,
            @Value("${jwt.lifetime}") long expirationTime,
            UserRepository userRepository
    ) {
        this.key = Keys.hmacShaKeyFor(key.getBytes());
        this.expirationTime = expirationTime;
        this.userRepository = userRepository;
    }

    public boolean isTokenValid(String token) {
        try {
            Date dateExpiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return dateExpiration != null && !dateExpiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(String login, UserRole role) {
        return Jwts.builder()
                .subject(login)
                .claim("id",
                        userRepository.findByLogin(login)
                                .orElseThrow(() -> new NoSuchElementException(
                                        String.format("User with login=%s not found", login))
                                )
                                .getId()
                )
                .claim("role", role.name())
                .signWith(key)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public String getLoginByToken(String token) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getRoleByToken(String token) {
        return (String) Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role");
    }
}
