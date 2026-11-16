package com.ateeb.enterprise_platform.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantRateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public static final int DEFAULT_TENANT_LIMIT = 10;
    public static final int DEFAULT_WINDOW_SECONDS = 10;

    private static final String RATE_LIMIT_LUA_SCRIPT = """
        local key = KEYS[1]
        local limit = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])

        local current = redis.call('INCR', key)
        if current == 1 then
            redis.call('EXPIRE', key, window)
        end

        local ttl = redis.call('TTL', key)
        return { current, ttl }
    """;

    private final DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(RATE_LIMIT_LUA_SCRIPT, List.class);

    public record RateLimitResult(boolean allowed, long remaining, long retryAfterSeconds) {}

    public RateLimitResult checkLimit(Long companyId) {
        String redisKey = "rate_limit:tenant:" + companyId;

        try {
            List<Long> result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(redisKey),
                    String.valueOf(DEFAULT_TENANT_LIMIT),
                    String.valueOf(DEFAULT_WINDOW_SECONDS)
            );

            if (result != null && result.size() >= 2) {
                long currentCount = result.get(0);
                long ttl = result.get(1);

                if (currentCount <= DEFAULT_TENANT_LIMIT) {
                    long remaining = Math.max(0, DEFAULT_TENANT_LIMIT - currentCount);
                    return new RateLimitResult(true, remaining, 0);
                } else {
                    return new RateLimitResult(false, 0, Math.max(1, ttl));
                }
            }
        } catch (Exception ex) {
            log.error("Redis rate limiter unavailable, failing open: {}", ex.getMessage());
            return new RateLimitResult(true, 1, 0);
        }

        return new RateLimitResult(true, 1, 0);
    }
}