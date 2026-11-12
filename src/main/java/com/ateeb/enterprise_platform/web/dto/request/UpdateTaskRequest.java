package com.ateeb.enterprise_platform.web.dto.request;

import com.ateeb.enterprise_platform.domain.enums.TaskPriority;
import com.ateeb.enterprise_platform.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
        String title,

        String description,

        @NotNull(message = "Task status is required")
        TaskStatus status,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        LocalDate dueDate,
        String assigneeEmail
) {}