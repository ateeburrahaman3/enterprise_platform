package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.config.PlatformDataSeeder;
import com.ateeb.enterprise_platform.domain.entity.Company;
import com.ateeb.enterprise_platform.domain.entity.Role;
import com.ateeb.enterprise_platform.domain.entity.User;
import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import com.ateeb.enterprise_platform.repository.CompanyRepository;
import com.ateeb.enterprise_platform.repository.RoleRepository;
import com.ateeb.enterprise_platform.repository.UserRepository;
import com.ateeb.enterprise_platform.web.dto.request.CreateCompanyRequest;
import com.ateeb.enterprise_platform.web.dto.response.CompanyProvisionResponse;
import com.ateeb.enterprise_platform.web.dto.response.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String DEFAULT_ADMIN_ROLE = "ADMIN";
    public static final String DEFAULT_STARTING_PASSWORD = "12345678";

    @Transactional
    public CompanyProvisionResponse provisionCompany(CreateCompanyRequest request){
        if(companyRepository.existsByName(request.companyName())){
            throw new IllegalArgumentException("Company with name '" + request.companyName() + "' already exists");
        }

        if(userRepository.existsByEmail(request.adminEmail())){
            throw new IllegalArgumentException("User with email '" + request.adminEmail() + "' is already registered");
        }

        Company company = companyRepository.save(new Company(request.companyName(), request.adminEmail()));

        Role adminRole = new Role(DEFAULT_ADMIN_ROLE, company);

        Set<AppPermission> tenantPermissions = Arrays.stream(AppPermission.values())
                .filter(p->
                        !p.name().startsWith("PLATFORM_"))
                .collect(Collectors.toSet());

        adminRole.setPermissions(tenantPermissions);
        roleRepository.save(adminRole);

        User adminUser = new User(
                request.adminEmail(),
                passwordEncoder.encode(DEFAULT_STARTING_PASSWORD),
                company,
                true
        );

        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        return new CompanyProvisionResponse(
                company.getId(),
                company.getName(),
                company.getAdminEmail(),
                company.isActive(),
                DEFAULT_STARTING_PASSWORD,
                adminUser.isMustChangePassword(),
                company.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies(){
        return companyRepository.findAll().stream()
                .map(c -> new CompanyResponse(
                        c.getId(),
                        c.getName(),
                        c.getAdminEmail(),
                        c.isActive(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyByName(String companyName) {
        Company company = companyRepository.findByName(companyName)
                .orElseThrow(() -> new IllegalArgumentException("Company '" + companyName + "' not found"));

        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getAdminEmail(),
                company.isActive(),
                company.getCreatedAt()
        );
    }

    @Transactional
    public void updateCompanyStatus(String companyName,boolean active){
        Company company = companyRepository.findByName(companyName)
                .orElseThrow(()-> new
                        IllegalArgumentException("Company '" + companyName + "' not found"));

        if(company.getName().equals(PlatformDataSeeder.SYSTEM_COMPANY_NAME) && !active){
            throw new IllegalArgumentException("The Root System Company cannot be deactivated");
        }

        company.setActive(active);
    }

}
