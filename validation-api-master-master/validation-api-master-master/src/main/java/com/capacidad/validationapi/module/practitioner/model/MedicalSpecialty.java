package com.capacidad.validationapi.module.practitioner.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPractice;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medical_specialty")
@NoArgsConstructor
@Getter
@Setter
public class MedicalSpecialty extends BaseEntity<Long> {

    @Column(unique = true, nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_specialty_type_id", nullable = false)
    private MedicalSpecialtyType medicalSpecialtyType;

    @ManyToMany(mappedBy = "medicalSpecialties")
    private Set<Practitioner> practitioners = new HashSet<>();

    @ManyToMany(mappedBy = "medicalSpecialties")
    private Set<MedicalPractice> medicalPractices = new HashSet<>();

}
