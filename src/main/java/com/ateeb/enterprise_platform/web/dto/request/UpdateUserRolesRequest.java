package com.ateeb.enterprise_platform.web.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty(message = "User must retain at least one role name")
        Set<String> roleNames,

        boolean confirmAdminCreation
) {}