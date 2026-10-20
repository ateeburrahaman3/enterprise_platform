package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.service.UserService;
import com.ateeb.enterprise_platform.web.dto.request.CreateTenantUserRequest;
import com.ateeb.enterprise_platform.web.dto.response.TenantUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createTenantUser(currentUser, request));
    }
}