package com.capacidad.validationapi.module.procedure.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "certificate_type")
@NoArgsConstructor
@Getter
@Setter
public class CertificateType extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

}
