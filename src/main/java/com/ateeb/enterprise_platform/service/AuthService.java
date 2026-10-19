package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.security.JwtService;
import com.ateeb.enterprise_platform.web.dto.request.LoginRequest;
import com.ateeb.enterprise_platform.web.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthResponse login(LoginRequest request) {
        // 1. Delegate authentication to Spring's AuthenticationManager
        // This invokes CustomUserDetailsService and verifies BCrypt password automatically
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Extract our authenticated CustomUserDetails principal
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. Generate the signed JWT token with all tenant claims
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000, // Return seconds
                userDetails.getUsername(),
                userDetails.isMustChangePassword()
        );
    }
}