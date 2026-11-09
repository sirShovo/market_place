package application.infrastructure.security;

import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JJWT-backed implementation of {@link JwtServicePort} plus token parsing helpers
 * used by {@link JwtAuthenticationFilter}.
 */
@Component
public class JjwtAdapter implements JwtServicePort {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JjwtAdapter(
            @Value("${app.jwt.secret:MarketPlaceSuperSecretKeyForJWTSignature2024Minimum256BitsRequired!}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiry);

        if (user.getRole() != null) {
            builder.claim("role", user.getRole().getCode());
        }
        if (user.getIdentification() != null) {
            builder.claim("identification", user.getIdentification().value());
        }

        return builder.signWith(signingKey).compact();
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String extractIdentification(String token) {
        return parseClaims(token).get("identification", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
