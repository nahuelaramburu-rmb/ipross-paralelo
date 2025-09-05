package com.capacidad.validationapi.module.medicalcoverage.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "charge_type")
@NoArgsConstructor
@Getter
@Setter
public class ChargeType extends BaseEntity<Long> {

    @Column(unique = true, nullable = false)
    private String name;

}
