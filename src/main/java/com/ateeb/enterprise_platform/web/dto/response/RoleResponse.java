package com.ateeb.enterprise_platform.web.dto.response;

import com.ateeb.enterprise_platform.domain.enums.AppPermission;

import java.util.Set;

public record RoleResponse(
        Long id,
        String name,
        Long companyId,
        Set<AppPermission> permissions
) {}