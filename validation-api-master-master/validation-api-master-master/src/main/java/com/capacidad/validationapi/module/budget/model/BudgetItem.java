package com.capacidad.validationapi.module.budget.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budget_item")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "budget_item_seq")
@Relation(collectionRelation = "budgetItems")
public class BudgetItem extends BaseTenantEntity<Long> {


    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false, updatable = false)
    private Budget budget;


    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medical_authorization_id", nullable = false, updatable = false)
    private MedicalAuthorization medicalAuthorization;

    @Audited
    @Column
    private BigDecimal chargeSubtotal;

    @Audited
    @Column
    private BigDecimal chargeUnitPrice;

    @Audited
    @Column(nullable = false, updatable = false)
    private Integer quantity = 1;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false, updatable = false)
    private Nomenclator nomenclator;

}
