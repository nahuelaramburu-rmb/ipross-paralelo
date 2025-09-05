package com.capacidad.validationapi.module.premedicalauthorization.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pre_medical_authorization_item")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "pre_medical_authorization_item_seq")
public class PreMedicalAuthorizationItem extends BaseTenantEntity<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false, updatable = false)
    private Nomenclator nomenclator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_medical_authorization_id", nullable = false, updatable = false)
    private PreMedicalAuthorization preMedicalAuthorization;

    @Column(nullable = false, updatable = false)
    private Integer quantity = 1;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private Integer remaining = 1;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean auditing = false;

    @Column(name = "charge_unit_price", updatable = false)
    private BigDecimal chargeUnitPrice;


}
