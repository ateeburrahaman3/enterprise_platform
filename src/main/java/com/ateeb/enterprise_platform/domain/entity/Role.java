package com.ateeb.enterprise_platform.domain.entity;

import com.ateeb.enterprise_platform.domain.enums.AppPermission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_role_name",
                        columnNames = {
                                "company_id","name"
                        }
                )
        }
)
@Getter @Setter @NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(nullable = false,length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "company_id",nullable = false)
    private Company company;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(
                    name = "role_id",
                    nullable = false
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission",nullable = false,length = 50)
    private Set<AppPermission> permissions = new HashSet<>();

    public Role(String name, Company company) {
        this.name = name;
        this.company = company;
    }

    public void addPermission(AppPermission permission) {
        this.permissions.add(permission);
    }

}
