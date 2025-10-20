package com.capacidad.validationapi.module.disease.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@Table(name = "icd10_disease")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "icd10_disease_seq", allocationSize = 1)
@Relation(collectionRelation = "icd10Diseases")
public class ICD10Disease extends BaseEntity<Long> {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne
    @JoinColumn(name = "icd10_category_id", nullable = false)
    private ICD10Category icd10Category;

}
