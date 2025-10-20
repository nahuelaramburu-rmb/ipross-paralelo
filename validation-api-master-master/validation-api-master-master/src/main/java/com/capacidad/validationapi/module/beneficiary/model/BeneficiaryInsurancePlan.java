package com.capacidad.validationapi.module.beneficiary.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "beneficiary_insurance_plan",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"insurance_plan_id", "beneficiary_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"beneficiary_id", "priority", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "beneficiary_insurance_plan_seq", allocationSize = 1)
public class BeneficiaryInsurancePlan extends BaseTenantEntity<Long> {

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_plan_id", nullable = false)
    private InsurancePlan insurancePlan;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Audited(withModifiedFlag = true)
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private Integer priority;

}
