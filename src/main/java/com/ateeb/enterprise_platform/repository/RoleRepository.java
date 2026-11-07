package com.ateeb.enterprise_platform.repository;

import com.ateeb.enterprise_platform.domain.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCompanyIdAndName(Long companyId, String name);
    boolean existsByCompanyIdAndName(Long companyId, String name);
    List<Role> findAllByCompanyId(Long companyId);

    List<Role> findAllByCompanyIdAndNameIn(Long companyId,Set<String> roleNames);
}
