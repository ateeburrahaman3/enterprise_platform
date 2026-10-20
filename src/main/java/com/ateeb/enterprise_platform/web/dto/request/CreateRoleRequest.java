package com.ateeb.enterprise_platform.web.dto.request;

import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
        String name,

        @NotEmpty(message = "At least one permission must be assigned to the role")
        Set<AppPermission> permissions
) {}