package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.service.CompanyService;
import com.ateeb.enterprise_platform.web.dto.request.CreateCompanyRequest;
import com.ateeb.enterprise_platform.web.dto.response.CompanyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/companies")
@RequiredArgsConstructor
public class PlatformCompanyController {

    private final CompanyService companyService;

    @PostMapping

    @PreAuthorize("hasAuthority('PLATFORM_COMPANY_CREATE')")
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CreateCompanyRequest request){

        CompanyResponse response = companyService.provisionCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

