package com.capacidad.validationapi.module.medicalcoverage.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionType;
import com.capacidad.validationapi.module.medicalcoverage.reference.ChargeTypeReference;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.person.model.Gender;
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

@Entity
@Table(name = "medical_coverage_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"nomenclator_id", "medical_coverage_id", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "medical_coverage_item_seq", allocationSize = 1)
@Relation(collectionRelation = "medicalCoverageItems")
public class MedicalCoverageItem extends BaseTenantEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_coverage_id", nullable = false)
    private MedicalCoverage medicalCoverage;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restriction_type_id")
    private RestrictionType restrictionType;

    @Audited(withModifiedFlag = true)
    @Column(columnDefinition = "boolean DEFAULT false")
    private Boolean auditRequired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false)
    private Nomenclator nomenclator;

    @Audited(withModifiedFlag = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Audited(withModifiedFlag = true)
    @Column(name = "age_from")
    private Integer ageFrom;

    @Audited(withModifiedFlag = true)
    @Column(name = "age_to")
    private Integer ageTo;

    @Audited(withModifiedFlag = true)
    @Column(name = "await_days", nullable = false)
    private Integer awaitDays;

    @Audited(withModifiedFlag = true)
    @Column(name = "fixed_max_quantity")
    private Integer fixedMaxQuantity;

    @Audited(withModifiedFlag = true)
    @Column(name = "fixed_max_days")
    private Integer fixedMaxDays;

    @Audited(withModifiedFlag = true)
    @Column(name = "free_max_quantity")
    private Integer freeMaxQuantity;

    @Audited(withModifiedFlag = true)
    @Column(name = "free_max_days")
    private Integer freeMaxDays;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_type_id")
    private ChargeType chargeType;

    @Audited(withModifiedFlag = true)
    @Column(name = "charge_value")
    private BigDecimal chargeValue;

    @OneToMany(mappedBy = "medicalCoverageItem")
    @BatchSize(size = 20)
    private Set<MedicalAuthorizationItem> medicalAuthorizationItems = new HashSet<>();

    public boolean isChargeTypePercentage() {
        return this.getChargeType() != null && this.getChargeType().getId().equals(ChargeTypeReference.PERCENTAGE.getId());
    }

    public boolean isChargeTypeFixedValue() {
        return this.getChargeType() != null && this.getChargeType().getId().equals(ChargeTypeReference.FIXED_VALUE.getId());
    }

    public boolean hasFreePracticesConfigured() {
        return this.getFreeMaxQuantity() != null && this.getFreeMaxDays() != null;
    }

}
