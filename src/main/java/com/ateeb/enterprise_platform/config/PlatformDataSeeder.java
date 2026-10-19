package com.ateeb.enterprise_platform.config;

import com.ateeb.enterprise_platform.domain.entity.Company;
import com.ateeb.enterprise_platform.domain.entity.Role;
import com.ateeb.enterprise_platform.domain.entity.User;
import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import com.ateeb.enterprise_platform.repository.CompanyRepository;
import com.ateeb.enterprise_platform.repository.RoleRepository;
import com.ateeb.enterprise_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformDataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${platform.bootstrap.admin-email}")
    private String adminEmail;
    @Value("${platform.bootstrap.admin-password}")
    private String adminPassword;

    public static final String SYSTEM_COMPANY_NAME = "SYSTEM_PLATFORM";
    public static final String SUPER_ADMIN_ROLE_NAME = "SYSTEM_ADMIN";

    public void run(String... args){

        Company systemCompany = companyRepository.findByName(SYSTEM_COMPANY_NAME)
                .orElseGet(()->{
                    log.info("Creating Root System Tenant {}", SYSTEM_COMPANY_NAME);
                    return companyRepository.save(new
                            Company(SYSTEM_COMPANY_NAME));
                });

        Role superAdminRole = roleRepository.findByCompanyIdAndName(
                systemCompany.getId(),SUPER_ADMIN_ROLE_NAME)
                .orElseGet(()->{
                    log.info("Creating Role: {} with all platform permissions", SUPER_ADMIN_ROLE_NAME);
                    Role role = new
                            Role(SUPER_ADMIN_ROLE_NAME,systemCompany);

                    role.setPermissions(Set.of(
                            AppPermission.PLATFORM_COMPANY_CREATE,
                            AppPermission.PLATFORM_COMPANY_READ,
                            AppPermission.PLATFORM_COMPANY_UPDATE
                    ));
                    return roleRepository.save(role);
                });

        if(!userRepository.existsByEmail(adminEmail)){
            log.info("Bootstrapping Platform Developer Account: {}", adminEmail);

            User developer = new User(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    systemCompany,
                    false
            );

            developer.addRole(superAdminRole);
            userRepository.save(developer);
            log.info("Platform Developer successfully provisioned.");
        }
    }
}
