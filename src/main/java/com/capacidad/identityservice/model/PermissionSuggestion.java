package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "application_permission_suggestion")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "permission_seq_gen", sequenceName = "application_permission_suggestion_seq", allocationSize = 1)
public class PermissionSuggestion extends BaseEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "permission_seq_gen")
    private Long id;


    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "permissionSuggestion", cascade = CascadeType.ALL)
    private Set<ApplicationUserContext> userContextSet = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @BatchSize(size = 50)
    @JoinTable(
            name = "application_permission_suggestion_group",
            joinColumns = {@JoinColumn(name = "application_permission_suggestion_id")},
            inverseJoinColumns = {@JoinColumn(name = "application_permission_group_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"application_permission_suggestion_id", "application_permission_group_id"})
            }
    )
    private List<PermissionGroup> permissionGroups = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_role_id", nullable = false)
    private Role role;

    @Override
    public void associateChildObjects() {
        this.getUserContextSet().forEach(c -> c.setPermissionSuggestion(this));
    }

}
