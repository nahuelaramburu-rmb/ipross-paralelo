package com.capacidad.validationapi.module.medicalauthorization.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "medical_authorization_item")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "medical_authorization_item_seq")
@Relation(collectionRelation = "authorizationItems")
public class MedicalAuthorizationItem extends BaseTenantEntity<Long> {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false, updatable = false)
    private Nomenclator nomenclator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_authorization_id", nullable = false, updatable = false)
    private MedicalAuthorization medicalAuthorization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_coverage_item_id", updatable = false)
    private MedicalCoverageItem medicalCoverageItem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_item_id")
    private BatchItem batchItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_item", updatable = false)
    private ContractItem contractItem;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    @Audited(withModifiedFlag = true, targetAuditMode = NOT_AUDITED)
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "medical_authorization_item_failure",
            joinColumns = {@JoinColumn(name = "medical_authorization_item_id")},
            inverseJoinColumns = {@JoinColumn(name = "failure_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"failure_id"})
            }
    )
    @BatchSize(size = 20)
    private Set<Failure> failures = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorization_condition_id")
    private AuthorizationCondition authorizationCondition;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private Integer quantity = 1;


    @Column(name = "charge_subtotal")
    private BigDecimal chargeSubtotal;


    @Column(name = "charge_unit_price")
    private BigDecimal chargeUnitPrice;

    @Audited(withModifiedFlag = true)
    @Column
    private String resolution;


    @Column
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_event_type")
    private CalendarEventType calendarEventType;


    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean settled = false;

    @Audited(withModifiedFlag = true)
    @Column(columnDefinition = "boolean DEFAULT false", nullable = false)
    private Boolean refundable = false;

    public boolean isApproved() {
        return this.getStatus().getId().equals(VALIDATION_APPROVED.getId());
    }

    public boolean isPending() {
        return this.getStatus().getId().equals(VALIDATION_PENDING.getId());
    }

    public boolean isRejected() {
        return this.getStatus().getId().equals(VALIDATION_REJECTED.getId());
    }

    public boolean containsBatchItem() {
        return this.getBatchItem() != null;
    }

    public boolean containsFailures() {
        return !this.getFailures().isEmpty();
    }

}
