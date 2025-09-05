package com.capacidad.validationapi.module.contract.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "module")
@NoArgsConstructor
@Getter
@Setter
public class Module extends BaseEntity<Long> {

    @Column(unique = true, nullable = false)
    private String name;

}
