package com.ateeb.enterprise_platform.service;

import com.ateeb.enterprise_platform.domain.entity.Company;
import com.ateeb.enterprise_platform.domain.entity.Task;
import com.ateeb.enterprise_platform.domain.entity.User;
import com.ateeb.enterprise_platform.domain.enums.TaskStatus;
import com.ateeb.enterprise_platform.repository.CompanyRepository;
import com.ateeb.enterprise_platform.repository.TaskRepository;
import com.ateeb.enterprise_platform.repository.UserRepository;
import com.ateeb.enterprise_platform.security.CustomUserDetails;
import com.ateeb.enterprise_platform.web.dto.request.CreateTaskRequest;
import com.ateeb.enterprise_platform.web.dto.request.UpdateTaskRequest;
import com.ateeb.enterprise_platform.web.dto.response.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(CustomUserDetails currentUser, CreateTaskRequest request) {
        Long companyId = currentUser.getCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new
                        IllegalArgumentException("Company Not Found"));

        User assignee = null;
        if (request.assigneeEmail() != null && !request.assigneeEmail().isBlank()) {
            assignee = userRepository.findByCompanyIdAndEmail(companyId, request.assigneeEmail())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Cannot assign task: User '" + request.assigneeEmail() + "' does not exist in your company"
                    ));
        }

        Task task = new Task(
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate(),
                company,
                assignee,
                currentUser.getUsername()
        );
        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getCompanyTasks(CustomUserDetails currentUser, TaskStatus status, Pageable pageable) {
        Long companyId = currentUser.getCompanyId();

        Page<Task> taskPage;
        if (status != null) {
            taskPage = taskRepository.findAllByCompanyIdAndStatus(companyId, status, pageable);
        } else {
            taskPage = taskRepository.findAllByCompanyId(companyId, pageable);
        }

        return taskPage.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(CustomUserDetails currentUser, Long taskId) {
        Task task = taskRepository.findByCompanyIdAndId(currentUser.getCompanyId(), taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(CustomUserDetails currentUser, Long taskId, UpdateTaskRequest request) {
        Long companyId = currentUser.getCompanyId();

        Task task = taskRepository.findByCompanyIdAndId(companyId, taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        User assignee = null;
        if (request.assigneeEmail() != null && !request.assigneeEmail().isBlank()) {
            assignee = userRepository.findByCompanyIdAndEmail(companyId, request.assigneeEmail())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Cannot assign task: User '" + request.assigneeEmail() + "' does not exist in your company"
                    ));
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setAssignee(assignee);

        return toResponse(task);
    }

    @Transactional
    public void deleteTask(CustomUserDetails currentUser, Long taskId) {
        Task task = taskRepository.findByCompanyIdAndId(currentUser.getCompanyId(), taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        taskRepository.delete(task);
    }


    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCompany().getId(),
                task.getAssignee() != null ? task.getAssignee().getEmail() : null,
                task.getCreatorEmail(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}