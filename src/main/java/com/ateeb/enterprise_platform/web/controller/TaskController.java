package com.ateeb.enterprise_platform.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/tasks")
public class TaskController {

    @GetMapping
    @PreAuthorize("hasAuthority('TASK_READ') and @tenantGuard.isSameTenant(#companyId)")
    public ResponseEntity<List<Map<String, Object>>> getTasks(@PathVariable Long companyId) {

        return ResponseEntity.ok(List.of(
                Map.of("id", 101, "title", "Audit Security Logs", "status", "IN_PROGRESS"),
                Map.of("id", 102, "title", "Prepare Q3 Architecture Deck", "status", "TODO")
        ));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_CREATE') and @tenantGuard.isSameTenant(#companyId)")
    public ResponseEntity<Map<String, Object>> createTask(
            @PathVariable Long companyId,
            @RequestBody Map<String, String> request
    ) {
        return ResponseEntity.ok(Map.of(
                "id", 103,
                "companyId", companyId,
                "title", request.get("title"),
                "status", "CREATED"
        ));
    }
}