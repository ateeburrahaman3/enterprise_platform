package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.domain.entity.Company;
import com.ateeb.enterprise_platform.domain.entity.Role;
import com.ateeb.enterprise_platform.domain.entity.User;
import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import com.ateeb.enterprise_platform.repository.CompanyRepository;
import com.ateeb.enterprise_platform.repository.RoleRepository;
import com.ateeb.enterprise_platform.repository.UserRepository;
import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.web.dto.request.CreateTenantUserRequest;
import com.ateeb.enterprise_platform.web.dto.response.TenantUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String DEFAULT_STARTING_PASSWORD = "12345678";
    public static final String ADMIN_ROLE_NAME = "ADMIN";

    @Transactional
    public TenantUserResponse createTenantUser(CustomUserDetails currentUser, CreateTenantUserRequest request) {
        Long companyId = currentUser.getCompanyId();


        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email '" + request.email() + "' already exists");
        }


        List<Role> requestedRoles = roleRepository.findAllById(request.roleIds());
        if (requestedRoles.size() != request.roleIds().size()) {
            throw new IllegalArgumentException("One or more role IDs do not exist");
        }

        for (Role role : requestedRoles) {
            if (!role.getCompany().getId().equals(companyId)) {

                throw new AccessDeniedException("Access Denied: Role ID " + role.getId() + " does not belong to your company");
            }
        }


        Set<AppPermission> callerPermissions = currentUser.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(auth -> !auth.startsWith("ROLE_"))
                .map(AppPermission::valueOf)
                .collect(Collectors.toSet());

        boolean isAssigningAdmin = false;
        for (Role role : requestedRoles) {
            if (role.getName().equalsIgnoreCase(ADMIN_ROLE_NAME)) {
                isAssigningAdmin = true;
            }


            if (!callerPermissions.containsAll(role.getPermissions())) {
                throw new AccessDeniedException(
                        "Privilege Escalation Denied: You cannot assign role '" + role.getName() +
                                "' because it contains permissions you do not possess"
                );
            }
        }


        if (isAssigningAdmin && !request.confirmAdminCreation()) {
            throw new IllegalArgumentException(
                    "Assigning the ADMIN role requires explicit confirmation. Please set confirmAdminCreation to true."
            );
        }


        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found"));

        User newUser = new User(
                request.email(),
                passwordEncoder.encode(DEFAULT_STARTING_PASSWORD),
                company,
                true
        );

        requestedRoles.forEach(newUser::addRole);
        User savedUser = userRepository.save(newUser);

        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new TenantUserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                company.getId(),
                roleNames,
                savedUser.isMustChangePassword(),
                savedUser.getCreatedAt()
        );
    }
}