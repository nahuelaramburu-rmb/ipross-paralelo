package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "application_user_context", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"application_user_id", "application_tenant_id", "application_role_id"})}
)
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_user_context_seq")
public class ApplicationUserContext extends BaseEntity<Long> implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_user_id", nullable = false)
    private ApplicationUser user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_tenant_id", nullable = false)
    private Tenant tenant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_permission_suggestion_id")
    private PermissionSuggestion permissionSuggestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_strategy", nullable = false)
    private PermissionStrategy permissionStrategy;

    @ManyToMany(cascade = CascadeType.MERGE)
    @BatchSize(size = 50)
    @JoinTable(
            name = "application_context_permission_group",
            joinColumns = {@JoinColumn(name = "application_context_id")},
            inverseJoinColumns = {@JoinColumn(name = "application_permission_group_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"application_context_id", "application_permission_group_id"})
            }
    )
    private List<PermissionGroup> permissionGroups = new ArrayList<>();

    @Override
    public void associateChildObjects() {
        user.getContextSet().add(this);
    }

    public boolean containsPermissionGroups() {
        return permissionGroups != null && !permissionGroups.isEmpty();
    }

    public boolean containsPermissionSuggestion() {
        return permissionSuggestion != null;
    }

}
