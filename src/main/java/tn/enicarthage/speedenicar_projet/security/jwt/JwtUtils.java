package tn.enicarthage.speedenicar_projet.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtils {

    @Value("${speed.jwt.secret}")
    private String jwtSecret;

    @Value("${speed.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;     // 15 min

    @Value("${speed.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;    // 7 days

    // ── Token generation ────────────────────────────────────

    public String generateAccessToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return buildToken(claims, email, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String email) {
        return buildToken(new HashMap<>(), email, refreshTokenExpirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                //.expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token parsing ───────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── Validation ──────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        // Compare avec l'email, pas getUsername() qui retourne l'ID
        if (userDetails instanceof tn.enicarthage.speedenicar_projet.security.service.CustomUserDetails customUser) {
            return email.equals(customUser.getEmail()) && !isTokenExpired(token);
        }
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Signature JWT invalide: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformé: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expiré: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Claims JWT vides: {}", e.getMessage());
        }
        return false;
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            if (expiration == null) {
                return false;
            }
            return expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
