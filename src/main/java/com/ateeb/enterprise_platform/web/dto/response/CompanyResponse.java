package com.ateeb.enterprise_platform.web.dto.response;

import java.time.Instant;

public record CompanyResponse(
        Long companyId,
        String companyName,
        String adminEmail,
        String initialPassword,
        boolean mustChangePassword,
        Instant createdAt
) {}