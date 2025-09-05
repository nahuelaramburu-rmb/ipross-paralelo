package com.capacidad.validationapi.module.procedure.model;

import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "procedure_seq", allocationSize = 1)
@Relation(collectionRelation = "procedures")
public class CUDProcedure extends Procedure {

    @BatchSize(size = 20)
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "cud_procedure_diagnosis",
            joinColumns = {@JoinColumn(name = "procedure_id")},
            inverseJoinColumns = {@JoinColumn(name = "disease_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"procedure_id", "disease_id"})
            }
    )
    private Set<ICD10Disease> diagnosis = new HashSet<>();

}
