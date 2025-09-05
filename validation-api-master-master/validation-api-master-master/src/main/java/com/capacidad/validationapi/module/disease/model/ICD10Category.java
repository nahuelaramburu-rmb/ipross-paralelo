package com.capacidad.validationapi.module.disease.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@Table(name = "icd10_category")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "icd10_category_seq", allocationSize = 1)
@Relation(collectionRelation = "icd10Categories")
public class ICD10Category extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne
    @JoinColumn(name = "icd10_chapter_id", nullable = false)
    private ICD10Chapter icd10Chapter;

}
