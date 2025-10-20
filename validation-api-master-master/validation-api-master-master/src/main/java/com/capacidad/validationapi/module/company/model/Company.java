package com.capacidad.validationapi.module.company.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.location.model.Address;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "company",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "address_id", "deleted", "deletion_token", "tenant_id"})
        }
)
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "company_seq", allocationSize = 1)
@Relation(collectionRelation = "companies")
public class Company extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "company")
    @BatchSize(size = 20)
    private Set<Beneficiary> beneficiaries = new HashSet<>();

}
