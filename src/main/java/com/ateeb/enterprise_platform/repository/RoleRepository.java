package com.ateeb.enterprise_platform.repository;

import com.ateeb.enterprise_platform.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCompanyIdAndName(Long companyId, String name);
    boolean existsByCompanyIdAndName(Long companyId, String name);
}
