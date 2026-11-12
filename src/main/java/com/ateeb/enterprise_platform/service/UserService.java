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
import com.ateeb.enterprise_platform.web.dto.request.UpdateStatusRequest;
import com.ateeb.enterprise_platform.web.dto.response.TenantUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

        List<Role> roles = roleRepository.findAllByCompanyIdAndNameIn(companyId, request.roleNames());
        if (roles.size() != request.roleNames().size()) {
            Set<String> foundNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
            Set<String> missingNames = request.roleNames().stream()
                    .filter(name -> !foundNames.contains(name))
                    .collect(Collectors.toSet());
            throw new IllegalArgumentException("The following roles do not exist in your company: " + missingNames);
        }

        Set<String> validPermissionNames = Arrays.stream(AppPermission.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<AppPermission> callerPermissions = currentUser.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(validPermissionNames::contains)
                .map(AppPermission::valueOf)
                .collect(Collectors.toSet());

        boolean isAssigningAdmin = false;
        for (Role role : roles) {
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

        roles.forEach(newUser::addRole);
        User savedUser = userRepository.save(newUser);

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<TenantUserResponse> getCompanyUsers(CustomUserDetails currentUser) {
        return userRepository.findAllByCompanyId(currentUser.getCompanyId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantUserResponse getUserByEmail(CustomUserDetails currentUser, String email) {
        User user = userRepository.findByCompanyIdAndEmail(currentUser.getCompanyId(), email)
                .orElseThrow(() -> new IllegalArgumentException("User '" + email + "' not found in your company"));
        return toResponse(user);
    }

    @Transactional
    public TenantUserResponse updateUserStatus(CustomUserDetails currentUser, String email, UpdateStatusRequest request) {

        if (currentUser.getUsername().equalsIgnoreCase(email) && !request.enabled()) {
            throw new IllegalArgumentException("Self-Lockout Prohibited: You cannot deactivate your own account");
        }

        User user = userRepository.findByCompanyIdAndEmail(currentUser.getCompanyId(), email)
                .orElseThrow(() -> new IllegalArgumentException("User '" + email + "' not found in your company"));

        user.setEnabled(request.enabled());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(CustomUserDetails currentUser, String email) {
        if (currentUser.getUsername().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Self-Deletion Prohibited: You cannot delete your own account");
        }

        User user = userRepository.findByCompanyIdAndEmail(currentUser.getCompanyId(), email)
                .orElseThrow(() -> new IllegalArgumentException("User '" + email + "' not found in your company"));

        userRepository.delete(user);
    }

    private TenantUserResponse toResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new TenantUserResponse(
                user.getId(),
                user.getEmail(),
                user.getCompany().getId(),
                roleNames,
                user.isMustChangePassword(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}