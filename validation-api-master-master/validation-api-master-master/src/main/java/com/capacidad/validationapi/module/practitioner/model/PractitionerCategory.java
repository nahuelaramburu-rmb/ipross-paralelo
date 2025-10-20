package com.capacidad.validationapi.module.practitioner.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "practitioner_category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"})}
)
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "practitioner_category_seq", allocationSize = 1)
@Relation(collectionRelation = "practitionerCategories")
public class PractitionerCategory extends BaseTenantEntity<Long> {

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "practitionerCategory")
    @BatchSize(size = 20)
    private Set<Practitioner> practitioners = new HashSet<>();

    @OneToMany(mappedBy = "practitionerCategory")
    @BatchSize(size = 20)
    private Set<ContractItem> contractItems = new HashSet<>();

}
