package com.ateeb.enterprise_platform.repository;

import com.ateeb.enterprise_platform.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByCompanyId(Long companyId);
    Optional<User> findByCompanyIdAndEmail(Long companyId, String email);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.roles r WHERE r.company.id = :companyId AND r.name = :roleName")
    boolean isRoleAssignedToAnyUser(@Param("companyId") Long companyId, @Param("roleName") String roleName);

}
