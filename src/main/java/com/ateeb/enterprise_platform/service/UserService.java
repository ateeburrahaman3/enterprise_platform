package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.domain.entity.Company;
import com.ateeb.enterprise_platform.domain.entity.Role;
import com.ateeb.enterprise_platform.domain.entity.User;
import com.ateeb.enterprise_platform.repository.CompanyRepository;
import com.ateeb.enterprise_platform.repository.RoleRepository;
import com.ateeb.enterprise_platform.repository.UserRepository;
import com.ateeb.enterprise_platform.web.dto.request.CreateTenantUserRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateStatusRequest;
import com.ateeb.enterprise_platform.web.dto.response.TenantUserResponse;
import lombok.RequiredArgsConstructor;
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
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;

    public static final String DEFAULT_STARTING_PASSWORD = "12345678";

    @Transactional
    public TenantUserResponse createTenantUser(Long companyId, CreateTenantUserRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("User with email '" + request.email() + "' already exists");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Company not found with ID: " + companyId));

        List<Role> roles = roleRepository.findAllByCompanyIdAndNameIn(companyId,request.roleNames());

        if(roles.size() != request.roleNames().size()) {
            Set<String> foundNames = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            Set<String> missingNames = request.roleNames()
                    .stream()
                    .filter(name -> !foundNames.contains(name))
                    .collect(Collectors.toSet());

            throw new IllegalArgumentException("The following roles do not exist in this company: " + missingNames);
        }
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
    public List<TenantUserResponse> getCompanyUsers(Long companyId){
        return userRepository.findAllByCompanyId(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantUserResponse getUserByEmail(Long companyId, String email){
        User user = userRepository.findByCompanyIdAndEmail(companyId,email)
                .orElseThrow(() -> new
                        IllegalArgumentException("User '" + email + "' not found in this company"));
        return toResponse(user);
    }

    @Transactional
    public TenantUserResponse updateUserStatus(Long companyId, String email, UpdateStatusRequest request,String callerEmail){
        if(callerEmail.equalsIgnoreCase(email) && !request.enabled()){
            throw new IllegalArgumentException("Self-Lockout Prohibited: You cannot deactivate your own account");
        }

        User user = userRepository.findByCompanyIdAndEmail(companyId,email)
                .orElseThrow(() -> new
                        IllegalArgumentException("User '" + email + "' not found in this company"));

        user.setEnabled(request.enabled());
        return toResponse(userRepository.save(user));

    }

    public void deleteUser(Long companyId, String email, String callerEmail){
        if(callerEmail.equalsIgnoreCase(email)){
            throw new IllegalArgumentException("Self-Deletion Prohibited: You cannot delete your own account");
        }

        User user = userRepository.findByCompanyIdAndEmail(companyId,email)
                .orElseThrow(() -> new
                        IllegalArgumentException("User '" + email + "' not found in this company"));

        userRepository.delete(user);
    }

    private TenantUserResponse toResponse(User user){
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
