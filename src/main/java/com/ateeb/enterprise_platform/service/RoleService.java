package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.domain.entity.Company;
import com.ateeb.enterprise_platform.domain.entity.Role;
import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import com.ateeb.enterprise_platform.repository.CompanyRepository;
import com.ateeb.enterprise_platform.repository.RoleRepository;
import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.web.dto.request.CreateRoleRequest;
import com.ateeb.enterprise_platform.web.dto.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public RoleResponse createRole(CustomUserDetails currentUser, CreateRoleRequest request){
        Long companyId = currentUser.getCompanyId();

        if(roleRepository.existsByCompanyIdAndName(companyId,request.name())){
            throw new
                    IllegalStateException("Role with name '" + request.name() + "' already exists in this company");
        }

        Set<AppPermission> callerPermissions = currentUser.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(auth -> !auth.startsWith("ROLE_"))

                .map(AppPermission::valueOf)
                .collect(Collectors.toSet());

        if(!callerPermissions.containsAll(request.permissions())) {
            Set<AppPermission> illegalPermissions =
                    request.permissions().stream()
                            .filter(p->
                                    !callerPermissions.contains(p))
                    .collect(Collectors.toSet());
            throw new AccessDeniedException(
                    "Privilege Escalation Denied: You cannot grant permissions you do not possess: " + illegalPermissions
            );
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found"));

        Role newRole = new Role(request.name(), company);
        newRole.setPermissions(request.permissions());
        Role savedRole = roleRepository.save(newRole);

        return new RoleResponse(
                savedRole.getId(),
                savedRole.getName(),
                company.getId(),
                savedRole.getPermissions()
        );
    }
}
