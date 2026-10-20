package com.ateeb.enterprise_platform.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateTenantUserRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotEmpty(message = "User must be assigned at least one role ID")
        Set<Long> roleIds,

        boolean confirmAdminCreation
) {}