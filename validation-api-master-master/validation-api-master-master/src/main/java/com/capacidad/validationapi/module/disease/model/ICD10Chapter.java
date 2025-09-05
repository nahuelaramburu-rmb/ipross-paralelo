package com.capacidad.validationapi.module.disease.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "icd10_chapter")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "icd10_chapter_seq", allocationSize = 1)
@Relation(collectionRelation = "icd10Chapters")
public class ICD10Chapter extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String chapter;

    @Column(nullable = false, unique = true)
    private String description;

}
