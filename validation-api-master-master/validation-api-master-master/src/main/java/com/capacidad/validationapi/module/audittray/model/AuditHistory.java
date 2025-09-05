package com.capacidad.validationapi.module.audittray.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "audit_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"auditor_id", "audit_tray_id", "medical_authorization_id", "tenant_id"})
})
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "audit_history_seq")
public class AuditHistory extends BaseTenantEntity<Long> {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_tray_id", nullable = false)
    private AuditTray auditTray;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_audit_tray_id")
    private AuditTray fromAuditTray;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_auditor_id")
    private Auditor fromAuditor;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    private AuditTrayEvent event;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medical_authorization_id", nullable = false)
    private MedicalAuthorization medicalAuthorization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id")
    private Auditor auditor;

    @ManyToMany(mappedBy = "auditHistorySet", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 100)
    private Set<AuditHistoryItem> historyItems = new HashSet<>();

    @Override
    public void associateChildObjects() {
        this.historyItems.forEach(historyItem -> historyItem.getAuditHistorySet().add(this));
    }

    public boolean containsAuditor(Auditor auditor) {
        return this.getAuditTray().getAuditors().stream()
                .map(Auditor::getId)
                .anyMatch(i -> auditor.getId().equals(i));
    }

}
