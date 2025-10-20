package com.capacidad.validationapi.module.audittray.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "auditor",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sub", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "auditor_seq")
public class Auditor extends BaseTenantEntity<Long> {

    @Column(nullable = false, updatable = false)
    private UUID sub;

    @Column(nullable = false, updatable = false)
    private String username;

    @Column(name = "display_name", nullable = false, updatable = false)
    private String displayName;

    @ManyToMany(mappedBy = "auditors")
    private Set<AuditTray> auditTrays = new HashSet<>();

    @OneToMany(mappedBy = "auditor", cascade = CascadeType.ALL)
    @BatchSize(size = 20)
    private Set<AuditHistory> auditHistory = new HashSet<>();

    @Override
    public void associateChildObjects() {
        this.auditHistory.forEach(h -> {
            h.setAuditor(this);
            h.associateChildObjects();
        });
    }

}
