package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "application_role")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_role_seq")
public class Role extends BaseEntity<Long> implements Serializable {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "access_level", nullable = false)
    private Integer accessLevel;

    @Column(name = "resource_id_required", nullable = false)
    private Boolean resourceIdRequired;

    @Column(name = "reusable_resource_id", nullable = false)
    private Boolean reusableResourceId;


    public Role(String name) {
        this.name = name;
    }


    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "application_role_permission_group",
            joinColumns = {@JoinColumn(name = "application_role_id")},
            inverseJoinColumns = {@JoinColumn(name = "application_permission_group_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"application_role_id", "application_permission_group_id"})
            }
    )
    private List<PermissionGroup> permissionGroups = new ArrayList<>();

    @OneToMany(mappedBy = "role")
    private Set<PermissionSuggestion> permissionSuggestionSet = new HashSet<>();

}
