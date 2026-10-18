package com.ateeb.enterprise_platform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false,updatable = false)
    private Instant createdAt = Instant.now();

    public Company(String name){
        this.name = name;
        this.createdAt = Instant.now();
        this.active = true;
    }
}
