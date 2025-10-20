package com.capacidad.validationapi.module.audittray.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "audit_history_item")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "audit_history_item_seq")
public class AuditHistoryItem extends BaseTenantEntity<Long> {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_tray_id")
    private AuditTray auditTray;


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "audit_history_item_relation",
            joinColumns = {@JoinColumn(name = "audit_history_item_id")},
            inverseJoinColumns = {@JoinColumn(name = "audit_history_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"audit_history_item_id", "audit_history_id"})
            }
    )
    private Set<AuditHistory> auditHistorySet = new HashSet<>();


    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medical_authorization_item_id", nullable = false, updatable = false)
    private MedicalAuthorizationItem medicalAuthorizationItem;

    public AuditHistoryItem(MedicalAuthorizationItem medicalAuthorizationItem, AuditTray auditTray) {
        this.medicalAuthorizationItem = medicalAuthorizationItem;
        this.auditTray = auditTray;
    }

}
