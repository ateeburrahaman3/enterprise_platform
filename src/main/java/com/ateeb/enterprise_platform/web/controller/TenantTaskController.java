package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.domain.enums.TaskStatus;
import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.service.TaskService;
import com.ateeb.enterprise_platform.web.dto.request.CreateTaskRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateTaskRequest;
import com.ateeb.enterprise_platform.web.dto.response.TaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/tasks")
@RequiredArgsConstructor
public class TenantTaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_CREATE')")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TASK_READ')")
    public ResponseEntity<Page<TaskResponse>> getCompanyTasks(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getCompanyTasks(currentUser, status, pageable));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAuthority('TASK_READ')")
    public ResponseEntity<TaskResponse> getTaskById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(taskService.getTaskById(currentUser, taskId));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<TaskResponse> updateTask(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(currentUser, taskId, request));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAuthority('TASK_DELETE')")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long taskId
    ) {
        taskService.deleteTask(currentUser, taskId);
        return ResponseEntity.noContent().build();
    }
}