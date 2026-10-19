package com.ateeb.enterprise_platform.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank(message = "Company name is required")
        @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
        String companyName,

        @NotBlank(message = "Admin email is required")
        @Email(message = "Invalid email format")
        String adminEmail
) {}