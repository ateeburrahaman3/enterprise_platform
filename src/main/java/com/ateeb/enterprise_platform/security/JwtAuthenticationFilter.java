package com.ateeb.enterprise_platform.security;

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
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt =  authHeader.substring(7);

        try{
            if(tokenRevocationService.isTokenRevoked(jwt)){
                log.warn("Blocked request with revoked JWT token");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                        "error":"TOKEN_REVOKED",
                        "message": "This session has been logged out or revoked. Please log in again."
                        }
                        """);
                return;
            }

            final String userEmail = jwtService.extractUsername(jwt);

            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if(!userDetails.isEnabled()){
                    log.warn("Blocked request for disabled user or suspended company: {}", userEmail);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                    response.setContentType("application/json");
                    response.getWriter().write("""
                            {
                                "error": "ACCOUNT_DISABLED",
                                "message": "Your account or company has been deactivated."
                            }
                        """);
                    return;
                }

                if(jwtService.isTokenValid(jwt,userDetails.getUsername())){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }catch (Exception ex){
            log.error("Cannot set user authentication: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
