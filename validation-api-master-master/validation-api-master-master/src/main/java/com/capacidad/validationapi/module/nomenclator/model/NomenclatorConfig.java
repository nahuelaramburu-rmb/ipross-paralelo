package com.capacidad.validationapi.module.nomenclator.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "nomenclator_config")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "nomenclator_config_seq", allocationSize = 1)
public class NomenclatorConfig extends BaseTenantEntity<Long> {

    @Column(name = "report_required", nullable = false)
    private Boolean reportRequired;

    @Column(name = "expiration_days", nullable = false)
    private Integer expirationDays;

    @Column(name = "max_in_transaction", nullable = false)
    private Integer maxInTransaction;

}
