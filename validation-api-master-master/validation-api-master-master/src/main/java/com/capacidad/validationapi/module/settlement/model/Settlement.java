package com.capacidad.validationapi.module.settlement.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "settlement")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "settlement_seq")
@Relation(collectionRelation = "settlements")
public class Settlement extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "settlement", orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<SettlementItem> settlementItems = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_id", nullable = false, updatable = false)
    private Practitioner practitioner;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, updatable = false)
    private Contract contract;

    @Audited(withModifiedFlag = true)
    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Audited(withModifiedFlag = true)
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Override
    public void associateChildObjects() {
        this.settlementItems.forEach(settlementItem -> settlementItem.setSettlement(this));
    }

}