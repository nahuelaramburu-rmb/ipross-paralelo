package com.capacidad.validationapi.module.location.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
public class Country extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "phone_code", nullable = false, unique = true)
    private Integer phoneCode;

}
