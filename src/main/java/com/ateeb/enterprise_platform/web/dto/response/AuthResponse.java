package com.ateeb.enterprise_platform.web.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String email,
        boolean mustChangePassword
) {}