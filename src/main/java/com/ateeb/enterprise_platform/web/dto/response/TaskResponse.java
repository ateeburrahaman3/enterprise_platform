package com.ateeb.enterprise_platform.web.dto.response;

import com.ateeb.enterprise_platform.domain.enums.TaskPriority;
import com.ateeb.enterprise_platform.domain.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long companyId,
        String assigneeEmail,
        String creatorEmail,
        Instant createdAt,
        Instant updatedAt
) {}