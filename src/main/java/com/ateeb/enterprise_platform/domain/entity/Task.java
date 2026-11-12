package com.ateeb.enterprise_platform.domain.entity;

import com.ateeb.enterprise_platform.domain.enums.TaskPriority;
import com.ateeb.enterprise_platform.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(nullable = false,length = 150)
    private String creatorEmail;

    @Column(nullable = false,updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;


    public Task(String title,
                String description,
                TaskPriority priority,
                LocalDate dueDate,
                Company company,
                User assignee,
                String creatorEmail) {
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.status = TaskStatus.TODO;
        this.dueDate = dueDate;
        this.company = company;
        this.assignee = assignee;
        this.creatorEmail = creatorEmail;
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
