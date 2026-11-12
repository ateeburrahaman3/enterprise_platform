package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.service.UserService;
import com.ateeb.enterprise_platform.web.dto.request.CreateTenantUserRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateStatusRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateUserRolesRequest;
import com.ateeb.enterprise_platform.web.dto.response.TenantUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant/users")
@RequiredArgsConstructor
public class TenantUserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') and hasAuthority('USER_ASSIGN_ROLE')")
    public ResponseEntity<TenantUserResponse> createUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateTenantUserRequest request
    ) {
        TenantUserResponse response = userService.createTenantUser(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<TenantUserResponse>> getCompanyUsers(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(userService.getCompanyUsers(currentUser));
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<TenantUserResponse> getUserByEmail(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String email
    ) {
        return ResponseEntity.ok(userService.getUserByEmail(currentUser, email));
    }

    @PatchMapping("/{email}/status")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<TenantUserResponse> updateUserStatus(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String email,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(currentUser, email, request));
    }

    @PutMapping("/{email}/roles")
    @PreAuthorize("hasAuthority('USER_ASSIGN_ROLE')")
    public ResponseEntity<TenantUserResponse> updateUserRoles(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String email,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserRoles(currentUser, email, request));
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String email
    ) {
        userService.deleteUser(currentUser, email);
        return ResponseEntity.noContent().build();
    }
}