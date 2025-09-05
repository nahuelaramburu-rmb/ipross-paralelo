package com.capacidad.validationapi.module.beneficiary.model;

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
@Table(name = "beneficiary_category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "beneficiary_category_seq", allocationSize = 1)
@Relation(collectionRelation = "beneficiaryCategories")
public class BeneficiaryCategory extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "beneficiaryCategory")
    @BatchSize(size = 20)
    private Set<Beneficiary> beneficiaries = new HashSet<>();

}
