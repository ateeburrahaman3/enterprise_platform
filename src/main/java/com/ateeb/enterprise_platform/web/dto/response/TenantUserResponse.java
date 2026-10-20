package com.ateeb.enterprise_platform.web.dto.response;

import java.time.Instant;
import java.util.Set;

public record TenantUserResponse(
        Long userId,
        String email,
        Long companyId,
        Set<String> roleNames,
        boolean mustChangePassword,
        Instant createdAt
) {}