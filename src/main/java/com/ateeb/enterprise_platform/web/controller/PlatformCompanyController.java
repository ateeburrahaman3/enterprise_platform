package com.ateeb.enterprise_platform.web.controller;

import com.ateeb.enterprise_platform.service.CompanyService;
import com.ateeb.enterprise_platform.web.dto.request.CreateCompanyRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateStatusRequest;
import com.ateeb.enterprise_platform.web.dto.response.CompanyProvisionResponse;
import com.ateeb.enterprise_platform.web.dto.response.CompanyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform/companies")
@RequiredArgsConstructor
public class PlatformCompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyProvisionResponse> createCompany(
            @Valid @RequestBody CreateCompanyRequest request
    ){
        CompanyProvisionResponse response = companyService.provisionCompany(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies(){
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @PatchMapping("{companyName}/status")
    public ResponseEntity<Void> updateCompanyStatus(
            @PathVariable String companyName,
            @Valid @RequestBody UpdateStatusRequest request){
        companyService.updateCompanyStatus(companyName, request.enabled());

        return ResponseEntity.noContent().build();

    }
}
