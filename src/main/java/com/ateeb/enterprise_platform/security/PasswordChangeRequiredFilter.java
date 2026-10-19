package com.ateeb.enterprise_platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    private static final String CHANGE_PASSWORD_URI = "/api/auth/change-password";


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {

            if(userDetails.isMustChangePassword() &&
            !request.getRequestURI().equals(CHANGE_PASSWORD_URI)) {

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                response.getWriter().write("""
                        {
                        "error": "PASSWORD_CHANGE_REQUIRED",
                        "message": "You must change your default password before accessing the platform.",
                        "action": "POST /api/auth/change-password"
                        }
                        """);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
