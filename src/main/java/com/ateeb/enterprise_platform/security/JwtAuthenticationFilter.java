
package com.ateeb.enterprise_platform.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail == null || userEmail.isBlank()) {
                writeError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "INVALID_TOKEN",
                        "Invalid authentication token."
                );
                return;
            }

        } catch (JwtException | IllegalArgumentException ex) {

            log.warn("Invalid or expired JWT: {}", ex.getMessage());

            writeError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Your token is invalid or expired. Please log in again."
            );
            return;
        }

        try {

            if (tokenRevocationService.isTokenRevoked(jwt)) {

                log.warn("Blocked request with revoked JWT.");

                writeError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "TOKEN_REVOKED",
                        "This session has been logged out or revoked. Please log in again."
                );
                return;
            }

        } catch (TokenRevocationService.RedisUnavailableException ex) {

            if (!isLogoutRequest(request)) {

                log.error("Redis unavailable. Blocking authenticated request.");
                writeError(
                        response,
                        HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "REVOCATION_CHECK_UNAVAILABLE",
                        "Token verification is temporarily unavailable. Please try again or log in again."
                );
                return;
            }

            log.warn("Redis unavailable. Allowing logout endpoint to use MySQL fallback.");

        } catch (TokenRevocationService.DatabaseUnavailableException ex) {

            log.error("Database unavailable during token revocation verification.", ex);
            writeError(
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "REVOCATION_CHECK_UNAVAILABLE",
                    "Token verification is temporarily unavailable."
            );
            return;

        } catch (IllegalArgumentException ex) {

            writeError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Invalid token storage information."
            );
            return;

        } catch (TokenRevocationService.TokenRevocationUnavailableException ex) {

            log.error("Token revocation verification failed.", ex);

            writeError(
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "REVOCATION_CHECK_UNAVAILABLE",
                    "Token verification is temporarily unavailable."
            );
            return;
        }

        try {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userEmail);

            if (!jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                writeError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "INVALID_TOKEN",
                        "Invalid authentication token."
                );
                return;
            }

            if (!userDetails.isEnabled()) {

                log.warn(
                        "Blocked request for disabled user or suspended company: {}",
                        userEmail
                );

                writeError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "ACCOUNT_DISABLED",
                        "Your account or company has been deactivated."
                );
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }

        } catch (UsernameNotFoundException ex) {

            writeError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "USER_NOT_FOUND",
                    "User account not found."
            );
            return;

        } catch (JwtException | IllegalArgumentException ex) {

            writeError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Invalid or expired authentication token."
            );
            return;

        } catch (Exception ex) {

            log.error("Unable to establish user authentication.", ex);
            writeError(
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "AUTHENTICATION_UNAVAILABLE",
                    "Authentication is temporarily unavailable."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {

        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/auth/logout".equals(request.getServletPath());
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String error,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                """
                {
                    "error": "%s",
                    "message": "%s"
                }
                """.formatted(error, message)
        );
    }
}
