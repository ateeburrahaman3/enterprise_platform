package com.ateeb.enterprise_platform.repository;

import com.ateeb.enterprise_platform.domain.entity.Task;
import com.ateeb.enterprise_platform.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TaskRepository extends CrudRepository<Task, Long> {

    Page<Task> findAllByCompanyId(Long companyId, Pageable pageable);
    Page<Task> findAllByCompanyIdAndStatus(Long companyId, TaskStatus status, Pageable pageable);
    Optional<Task> findByCompanyIdAndId(Long companyId, Long id);

}
