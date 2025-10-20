package com.capacidad.validationapi.module.nomenclator.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "nomenclator_group",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "nomenclator_group_seq", allocationSize = 1)
@Relation(collectionRelation = "nomenclatorGroups")
public class NomenclatorGroup extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "nomenclator_group_relation",
            joinColumns = {@JoinColumn(name = "nomenclator_group_id")},
            inverseJoinColumns = {@JoinColumn(name = "nomenclator_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"nomenclator_id", "nomenclator_group_id"})
            }
    )
    @BatchSize(size = 20)
    private Set<Nomenclator> nomenclators = new HashSet<>();

}
