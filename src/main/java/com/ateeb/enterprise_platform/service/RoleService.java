package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.domain.entity.Company;
import com.ateeb.enterprise_platform.domain.entity.Role;
import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import com.ateeb.enterprise_platform.repository.CompanyRepository;
import com.ateeb.enterprise_platform.repository.RoleRepository;
import com.ateeb.enterprise_platform.repository.UserRepository;
import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.web.dto.request.CreateRoleRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateRolePermissionsRequest;
import com.ateeb.enterprise_platform.web.dto.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public static final String ROOT_ADMIN_ROLE = "ADMIN";

    @Transactional
    public RoleResponse createRole(CustomUserDetails currentUser, CreateRoleRequest request){
        Long companyId = currentUser.getCompanyId();

        if(roleRepository.existsByCompanyIdAndName(companyId,request.name())){
            throw new IllegalArgumentException("Role with name '" + request.name() + "' already exists in this company");
        }

        Set<String> validPermissionNames = Arrays.stream(AppPermission.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<AppPermission> callerPermissions = currentUser.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(validPermissionNames::contains)
                .map(AppPermission::valueOf)
                .collect(Collectors.toSet());

        if(!callerPermissions.containsAll(request.permissions())){
            Set<AppPermission> illegalPermissions = request.permissions().stream()
                    .filter(p -> !callerPermissions.contains(p))
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

    @Transactional(readOnly = true)
    public List<RoleResponse> getCompanyRoles(CustomUserDetails currentUser) {
        return roleRepository.findAllByCompanyId(currentUser.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(CustomUserDetails currentUser, String roleName) {
        Role role = roleRepository.findByCompanyIdAndName(currentUser.getCompanyId(), roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role '" + roleName + "' not found in your company"));
        return toResponse(role);
    }

    @Transactional
    public RoleResponse updateRolePermissions(CustomUserDetails currentUser, String roleName, UpdateRolePermissionsRequest request) {
        Long companyId = currentUser.getCompanyId();

        if (roleName.equalsIgnoreCase(ROOT_ADMIN_ROLE)) {
            throw new IllegalArgumentException("The root 'ADMIN' role permissions cannot be modified");
        }

        Role role = roleRepository.findByCompanyIdAndName(companyId, roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role '" + roleName + "' not found in your company"));

        validateNoPrivilegeEscalation(currentUser, request.permissions());

        role.setPermissions(request.permissions());
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(CustomUserDetails currentUser, String roleName) {
        Long companyId = currentUser.getCompanyId();

        if (roleName.equalsIgnoreCase(ROOT_ADMIN_ROLE)) {
            throw new IllegalArgumentException("The root 'ADMIN' role cannot be deleted");
        }

        Role role = roleRepository.findByCompanyIdAndName(companyId, roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role '" + roleName + "' not found in your company"));

        if (userRepository.isRoleAssignedToAnyUser(companyId, roleName)) {
            throw new IllegalStateException("Cannot delete role '" + roleName + "' because it is currently assigned to one or more active users");
        }

        roleRepository.delete(role);

    }

    private void validateNoPrivilegeEscalation(CustomUserDetails currentUser, Set<AppPermission> requestedPermissions) {
        Set<String> validPermissionNames = Arrays.stream(AppPermission.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<AppPermission> callerPermissions = currentUser.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(validPermissionNames::contains)
                .map(AppPermission::valueOf)
                .collect(Collectors.toSet());

        if (!callerPermissions.containsAll(requestedPermissions)) {
            Set<AppPermission> illegal = requestedPermissions.stream()
                    .filter(p -> !callerPermissions.contains(p))
                    .collect(Collectors.toSet());
            throw new AccessDeniedException("Privilege Escalation Denied: You cannot grant permissions you do not possess: " + illegal);
        }
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCompany().getId(),
                role.getPermissions()
        );
    }
}
