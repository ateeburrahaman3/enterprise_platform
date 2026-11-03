package com.ateeb.enterprise_platform.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false,length = 255)
    private String password;

    @Column(nullable = false)
    private boolean mustChangePassword = true;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    referencedColumnName = "id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id",
                    referencedColumnName = "id"
            )
    )
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false,updatable = false)
    private Instant createdAt = Instant.now();

    public User(String email, String password,Company company,boolean mustChangePassword) {
        this.email = email;
        this.password = password;
        this.company = company;
        this.mustChangePassword = mustChangePassword;
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }
}
