
package com.ateeb.enterprise_platform.security;

import com.ateeb.enterprise_platform.domain.entity.RevokedToken;
import com.ateeb.enterprise_platform.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private static final String REVOCATION_PREFIX = "revoked:token:";

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void revokeToken(String token) {

        String tokenId = jwtService.extractTokenId(token);

        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token ID is missing"
            );
        }

        long remainingMs = jwtService.getRemainingExpirationMs(token);

        if (remainingMs <= 0) {
            log.info("Token [{}] is already expired.", tokenId);
            return;
        }

        String store = getTokenStore(token);

        if (JwtService.DATABASE.equals(store)) {
            persistDatabaseRevocation(tokenId, remainingMs);
            return;
        }

        persistDatabaseRevocation(tokenId, remainingMs);

        try {
            redisTemplate.opsForValue().set(
                    REVOCATION_PREFIX + tokenId,
                    "revoked",
                    Duration.ofMillis(remainingMs)
            );

            log.info(
                    "Token [{}] revoked in Redis and MySQL.",
                    tokenId
            );

        } catch (RuntimeException ex) {

            log.warn(
                    "Redis unavailable while revoking token [{}]. " +
                            "MySQL revocation remains active.",
                    tokenId
            );
        }
    }

    public boolean isTokenRevoked(String token) {

        String tokenId = jwtService.extractTokenId(token);

        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token ID is missing"
            );
        }

        String store = getTokenStore(token);

        try {

            if (revokedTokenRepository.existsByTokenId(tokenId)) {
                return true;
            }

        } catch (RuntimeException ex) {

            throw new DatabaseUnavailableException(
                    "Unable to verify token revocation in database.",
                    ex
            );
        }

        // DATABASE tokens depend only on MySQL.
        if (JwtService.DATABASE.equals(store)) {
            return false;
        }

        try {

            return Boolean.TRUE.equals(
                    redisTemplate.hasKey(
                            REVOCATION_PREFIX + tokenId
                    )
            );

        } catch (RuntimeException ex) {

            throw new RedisUnavailableException(
                    "Redis is unavailable. Token revocation cannot be verified.",
                    ex
            );
        }
    }

    private String getTokenStore(String token) {

        String store = jwtService.extractRevocationStore(token);

        if (store == null || store.isBlank()) {
            return JwtService.REDIS;
        }

        if (!JwtService.REDIS.equals(store)
                && !JwtService.DATABASE.equals(store)) {

            throw new IllegalArgumentException(
                    "Invalid JWT revocation storage claim."
            );
        }

        return store;
    }

    private void persistDatabaseRevocation(
            String tokenId,
            long remainingMs
    ) {

        try {

            if (revokedTokenRepository.existsByTokenId(tokenId)) {
                return;
            }

            Instant expiresAt = Instant.now()
                    .plusMillis(remainingMs);

            RevokedToken revokedToken =
                    new RevokedToken(tokenId, expiresAt);

            revokedTokenRepository.save(revokedToken);

            log.info(
                    "Token [{}] revocation persisted in MySQL.",
                    tokenId
            );

        } catch (RuntimeException ex) {

            throw new DatabaseUnavailableException(
                    "Unable to persist token revocation in MySQL.",
                    ex
            );
        }
    }

    public static class TokenRevocationUnavailableException
            extends RuntimeException {

        public TokenRevocationUnavailableException(
                String message,
                Throwable cause
        ) {
            super(message, cause);
        }
    }

    public static class RedisUnavailableException
            extends TokenRevocationUnavailableException {

        public RedisUnavailableException(
                String message,
                Throwable cause
        ) {
            super(message, cause);
        }
    }

    public static class DatabaseUnavailableException
            extends TokenRevocationUnavailableException {

        public DatabaseUnavailableException(
                String message,
                Throwable cause
        ) {
            super(message, cause);
        }
    }
}
