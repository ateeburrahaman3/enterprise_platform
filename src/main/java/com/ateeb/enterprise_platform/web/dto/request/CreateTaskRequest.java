package com.ateeb.enterprise_platform.web.dto.request;

import com.ateeb.enterprise_platform.domain.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
        String title,

        String description,
        TaskPriority priority,
        LocalDate dueDate,
        String assigneeEmail
) {}