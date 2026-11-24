
package com.ateeb.enterprise_platform.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenStorageAvailabilityService {

    private final StringRedisTemplate redisTemplate;

    public String selectStoreForNewToken() {

        try {
            String response = redisTemplate.execute(
                    (RedisCallback<String>) connection -> connection.ping()
            );

            if ("PONG".equalsIgnoreCase(response)) {
                log.info("Redis is available. Selecting REDIS token storage.");
                return JwtService.REDIS;
            }

        } catch (DataAccessException ex) {
            log.warn(
                    "Redis is unavailable. Selecting DATABASE token storage. Reason: {}",
                    ex.getMessage()
            );
        }

        log.info("Selecting DATABASE token storage.");
        return JwtService.DATABASE;
    }
}
