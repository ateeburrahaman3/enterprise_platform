package com.ateeb.enterprise_platform.web.dto.request;

import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolePermissionsRequest(
        @NotEmpty(message = "Permissions set cannot be empty")
        Set<AppPermission> permissions
) {}