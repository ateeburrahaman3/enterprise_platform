package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.service.UserService;
import com.ateeb.enterprise_platform.web.dto.request.CreateTenantUserRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateStatusRequest;
import com.ateeb.enterprise_platform.web.dto.response.TenantUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant/users")
@RequiredArgsConstructor
public class TenantUserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<TenantUserResponse> createUser(
            @RequestParam Long companyId,
            @Valid @RequestBody CreateTenantUserRequest request
    ){
        TenantUserResponse response = userService.createTenantUser(companyId,request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TenantUserResponse>> getCompanyUsers(@RequestParam Long companyId){
        return ResponseEntity.ok(userService.getCompanyUsers(companyId));
    }

    @GetMapping("/{email}")
    public ResponseEntity<TenantUserResponse> getUserByEmail(
            @RequestParam Long companyId,
            @PathVariable String email
    ){
        return ResponseEntity.ok(userService.getUserByEmail(companyId,email));
    }

    @PatchMapping("/{email}/status")
    public ResponseEntity<TenantUserResponse> updateUserStatus(
            @RequestParam Long companyId,
            @PathVariable String email,
            @RequestHeader(value = "X-Caller-Email",
            defaultValue = "admin@platform.local") String callerEmail,
            @Valid @RequestBody UpdateStatusRequest request
    ){
        return ResponseEntity.ok(userService.updateUserStatus(companyId,email,request,callerEmail));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(
            @RequestParam Long companyId,
            @PathVariable String email,
            @RequestHeader(value = "X-Caller-Email",
            defaultValue = "admin@platform.local") String callerEmail
    ){
        userService.deleteUser(companyId,email,callerEmail);
        return ResponseEntity.noContent().build();
    }
}
