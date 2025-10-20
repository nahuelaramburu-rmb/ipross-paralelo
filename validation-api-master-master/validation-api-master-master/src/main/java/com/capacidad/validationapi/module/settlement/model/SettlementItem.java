package com.capacidad.validationapi.module.settlement.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
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
@Table(name = "settlement_item")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "settlement_item_seq")
@Relation(collectionRelation = "settlementItems")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class SettlementItem extends BaseTenantEntity<Long> {


    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "settlement_id", nullable = false, updatable = false)
    private Settlement settlement;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_center_id", nullable = false, updatable = false)
    private MedicalCenter medicalCenter;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_authorization_id", nullable = false, updatable = false)
    private MedicalAuthorization medicalAuthorization;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_authorization_item_id", nullable = false, updatable = false)
    private MedicalAuthorizationItem medicalAuthorizationItem;

    @Column
    private BigDecimal subtotal;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "charge_unit_price")
    private BigDecimal chargeUnitPrice;


    @Column(nullable = false, updatable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Boolean refundable = false;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false, updatable = false)
    private Nomenclator nomenclator;

}
