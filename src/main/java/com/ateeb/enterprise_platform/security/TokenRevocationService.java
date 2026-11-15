package com.ateeb.enterprise_platform.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {
    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;
    private static final String REVOCATION_PREFIX = "revoked:token:";

    public void revokeToken(String token) {
        try {
            String tokenId = jwtService.extractTokenId(token);
            long remainingMs = jwtService.getRemainingExpirationMs(token);

            if(tokenId != null && remainingMs > 0){
                String redisKey = REVOCATION_PREFIX + tokenId;

                redisTemplate.opsForValue().set(redisKey,"revoked", Duration.ofMillis(remainingMs));
                log.info("Token [{}] revoked in Redis for remaining {} ms", tokenId, remainingMs);
            }
        }catch (Exception ex){
            log.error("Failed to revoke token: {}", ex.getMessage());
        }
    }

    public boolean isTokenRevoked(String token) {
        try {
            String tokenId = jwtService.extractTokenId(token);
            if (tokenId == null) {
                return false;
            }

            return Boolean.TRUE.equals(redisTemplate.hasKey(REVOCATION_PREFIX + tokenId));
        }catch (Exception ex){
            log.error("Redis connection error while checking token revocation: {}", ex.getMessage());
            return false;
        }
    }
}
