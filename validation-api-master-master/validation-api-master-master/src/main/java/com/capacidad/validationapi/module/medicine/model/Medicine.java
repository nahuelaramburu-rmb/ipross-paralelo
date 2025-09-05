package com.capacidad.validationapi.module.medicine.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "medicine")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "medicine_seq", allocationSize = 1)
@Relation(collectionRelation = "medicines")
public class Medicine extends BaseTenantEntity<Long> {

    @Column(name = "exchange_id", nullable = false)
    private Long exchangeId;

    @Column(name = "exchange_plan_id", nullable = false)
    private Long exchangePlanId;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private String recommendation;

    @Column
    private String presentation;

    @Column
    private Integer units;

    @Column(name = "authorized_dosage")
    private BigDecimal authorizedDosage;

    @Column
    private BigDecimal concentration;

    @Column(name = "concentration_type_id")
    private Long concentrationTypeId;

    @Column(name = "product_type_id")
    private Long productTypeId;

    @Column(name = "units_type_id")
    private Long unitsTypeId;

    @Column(name = "drugId")
    private Long drugId;

    @Column(name = "size_id")
    private Long sizeId;

}
