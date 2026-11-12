package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.service.RoleService;
import com.ateeb.enterprise_platform.web.dto.request.CreateRoleRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateRolePermissionsRequest;
import com.ateeb.enterprise_platform.web.dto.response.RoleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant/roles")
@RequiredArgsConstructor
public class TenantRoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<RoleResponse> createRole(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateRoleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(currentUser, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<RoleResponse>> getCompanyRoles(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(roleService.getCompanyRoles(currentUser));
    }

    @GetMapping("/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<RoleResponse> getRoleByName(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String roleName
    ) {
        return ResponseEntity.ok(roleService.getRoleByName(currentUser, roleName));
    }

    @PutMapping("/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<RoleResponse> updateRolePermissions(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String roleName,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ResponseEntity.ok(roleService.updateRolePermissions(currentUser, roleName, request));
    }

    @DeleteMapping("/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> deleteRole(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String roleName
    ) {
        roleService.deleteRole(currentUser, roleName);
        return ResponseEntity.noContent().build();
    }
}