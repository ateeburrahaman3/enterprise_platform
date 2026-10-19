package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.service.AuthService;
import com.ateeb.enterprise_platform.web.dto.request.ChangePasswordRequest;
import com.ateeb.enterprise_platform.web.dto.request.LoginRequest;
import com.ateeb.enterprise_platform.web.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request){
        return ResponseEntity.ok(service.changePassword(currentUser,request));
    }

}