package com.capacidad.validationapi.module.practitioner.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "medical_specialty_type")
@NoArgsConstructor
@Getter
@Setter
public class MedicalSpecialtyType extends BaseEntity<Long> {

    @Column(unique = true, nullable = false)
    private String name;

}
