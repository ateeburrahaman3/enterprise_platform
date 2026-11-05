package com.ateeb.enterprise_platform.web.dto.response;

import java.time.Instant;

public record CompanyProvisionResponse(
        Long companyId,
        String companyName,
        String adminEmail,
        boolean active,
        String initialPassword,
        boolean mustChangePassword,
        Instant createdAt
) {
}
