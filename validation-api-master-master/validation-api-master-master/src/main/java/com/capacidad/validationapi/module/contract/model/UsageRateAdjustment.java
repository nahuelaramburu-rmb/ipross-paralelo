package com.capacidad.validationapi.module.contract.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_adjustment_seq", allocationSize = 1)
@Relation(collectionRelation = "adjustments")
public class UsageRateAdjustment extends ContractAdjustment {

    @Audited(withModifiedFlag = true)
    @Column(name = "usage_rate_threshold")
    private BigDecimal threshold;

    @Audited(withModifiedFlag = true)
    @Column(name = "capita_amount")
    private Long capitaAmount;

}
