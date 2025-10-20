package com.capacidad.validationapi.module.nomenclator.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medical_practice",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@Relation(collectionRelation = "medicalPractices")
public class MedicalPractice extends BaseTenantEntity<Long> {

    @Column(nullable = false, length = 500)
    private String name;

    @OneToMany(mappedBy = "medicalPractice")
    private Set<Nomenclator> nomenclators = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(
            name = "medical_specialties_practices",
            joinColumns = {@JoinColumn(name = "medical_practice_id")},
            inverseJoinColumns = {@JoinColumn(name = "medical_specialty_id")}
    )
    private Set<MedicalSpecialty> medicalSpecialties = new HashSet<>();

}
