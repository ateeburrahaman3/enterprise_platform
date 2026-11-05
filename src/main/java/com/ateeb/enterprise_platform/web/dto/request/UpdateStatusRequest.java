package com.ateeb.enterprise_platform.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @NotNull(message = "Active/Enabled status is required")
        boolean enabled
) {
}
