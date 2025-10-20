package com.capacidad.validationapi.module.ruleprocessor.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rule_configuration", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contract_id", "rule_id", "deleted", "deletion_token", "tenant_id"})
})
@NoArgsConstructor
@Getter
@Setter
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@SequenceGenerator(name = "base_seq_gen", sequenceName = "rule_configuration_seq", allocationSize = 1)
@Relation(collectionRelation = "ruleConfigurations")
public class RuleConfiguration extends BaseTenantEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule ruleRef;

    @Column(nullable = false, columnDefinition = "boolean DEFAULT true")
    private Boolean active;

    @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean applyToBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private Set<RuleData> data = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restriction_type_id")
    private RestrictionType restrictionType;

}
