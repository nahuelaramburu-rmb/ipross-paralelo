package com.capacidad.validationapi.module.nomenclator.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "medical_practice_type")
@NoArgsConstructor
@Getter
@Setter
public class MedicalPracticeType extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

}
