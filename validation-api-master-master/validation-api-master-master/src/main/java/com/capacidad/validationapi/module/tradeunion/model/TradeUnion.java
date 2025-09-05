package com.capacidad.validationapi.module.tradeunion.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.location.model.Address;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "trade_union",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"resource_id", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "trade_union_seq", allocationSize = 1)
@Relation(collectionRelation = "tradeUnions")
public class TradeUnion extends BaseTenantEntity<Long> {

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "resource_id", nullable = false, updatable = false)
    private UUID resourceId = UUID.randomUUID();

    @ManyToMany(mappedBy = "tradeUnions")
    @BatchSize(size = 20)
    private Set<Beneficiary> beneficiaries = new HashSet<>();

    @Audited(withModifiedFlag = true)
    @Column(name = "includes_family_group", columnDefinition = "boolean DEFAULT false", nullable = false)
    private Boolean includesFamilyGroup = false;

    @Audited(withModifiedFlag = true)
    @Column(name = "auto_settlement", columnDefinition = "boolean DEFAULT false", nullable = false)
    private Boolean autoSettlement = false;

    @Audited(withModifiedFlag = true)
    @Column(name = "day_of_settlement", columnDefinition = "integer DEFAULT 31", nullable = false)
    private Integer dayOfSettlement;

}
