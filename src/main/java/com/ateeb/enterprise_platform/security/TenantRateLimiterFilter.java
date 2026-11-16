package com.ateeb.enterprise_platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantRateLimiterFilter extends OncePerRequestFilter {

    private final TenantRateLimiterService rateLimiterService;

    public static final int HTTP_TOO_MANY_REQUESTS = 429;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails){
            Long companyId = userDetails.getCompanyId();

            if(companyId != null && !companyId.equals(1L)){
                TenantRateLimiterService.RateLimitResult result = rateLimiterService.checkLimit(companyId);

                response.setHeader("X-RateLimit-Limit",String.valueOf(TenantRateLimiterService.DEFAULT_TENANT_LIMIT));

                response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

                if(!result.allowed()){
                    log.warn("Rate limit exceeded for tenant company ID: {}", companyId);

                    response.setStatus(HTTP_TOO_MANY_REQUESTS);
                    response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));

                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("""
                        {
                            "error": "RATE_LIMIT_EXCEEDED",
                            "message": "Too many requests. You have exceeded your tenant API quota.",
                            "retryAfterSeconds": %d
                        }
                    """.formatted(result.retryAfterSeconds()));
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
