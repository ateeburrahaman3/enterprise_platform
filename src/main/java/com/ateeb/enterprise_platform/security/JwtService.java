
package com.ateeb.enterprise_platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String REDIS = "REDIS";
    public static final String DATABASE = "DATABASE";

    private static final String REVOCATION_STORE_CLAIM =
            "revocation_store";

    @Value("${application.security.jwt.secret-key}")
    private String secretKeyString;

    @Value("${application.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes =
                secretKeyString.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(
            CustomUserDetails userDetails,
            String revocationStore
    ) {

        if (!REDIS.equals(revocationStore)
                && !DATABASE.equals(revocationStore)) {
            throw new IllegalArgumentException(
                    "Invalid token revocation store"
            );
        }

        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("companyId", userDetails.getCompanyId())
                .claim("mustChangePassword",
                        userDetails.isMustChangePassword())
                .claim("authorities", authorities)
                .claim(REVOCATION_STORE_CLAIM, revocationStore)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractRevocationStore(String token) {
        return extractClaim(
                token,
                claims -> claims.get(
                        REVOCATION_STORE_CLAIM,
                        String.class
                )
        );
    }

    public long getRemainingExpirationMs(String token) {
        Date expiration =
                extractClaim(token, Claims::getExpiration);

        long remaining =
                expiration.getTime() - System.currentTimeMillis();

        return Math.max(0, remaining);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String userEmail) {
        final String username = extractUsername(token);

        return username.equals(userEmail)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration)
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
