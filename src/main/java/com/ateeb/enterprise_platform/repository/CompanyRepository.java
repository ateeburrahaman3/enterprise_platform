package com.ateeb.enterprise_platform.repository;

import com.ateeb.enterprise_platform.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company,Long> {

    Optional<Company> findByName(String name);
    boolean existsByName(String name);
}
