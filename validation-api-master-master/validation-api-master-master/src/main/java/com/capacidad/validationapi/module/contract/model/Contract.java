package com.capacidad.validationapi.module.contract.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.ruleprocessor.model.RuleConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "contract",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"contract_code", "deleted", "deletion_token", "tenant_id"}),
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_seq", allocationSize = 1)
@Relation(collectionRelation = "contracts")
public class Contract extends BaseTenantEntity<Long> {

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private String name;

    @Audited(withModifiedFlag = true)
    @Column(name = "contract_code")
    private String contractCode;

    @Audited(withModifiedFlag = true)
    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Audited(withModifiedFlag = true)
    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<ContractItem> contractItems = new HashSet<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<ContractAdjustment> contractAdjustments = new HashSet<>();

    @Audited(withModifiedFlag = true)
    @Column(name = "transit_condition", columnDefinition = "boolean DEFAULT false")
    private Boolean transitCondition = false;

    @Audited(withModifiedFlag = true)
    @Column(columnDefinition = "boolean DEFAULT true")
    private Boolean active = true;

    @Audited(withModifiedFlag = true)
    @Column(columnDefinition = "boolean DEFAULT false")
    private Boolean autoSettlement = false;

    @Audited(withModifiedFlag = true)
    @Column(name = "day_of_settlement", columnDefinition = "integer DEFAULT 31")
    private Integer dayOfSettlement = 31;

    @Column(name = "dtype", insertable = false, updatable = false)
    private String type;

    @ManyToMany(cascade = CascadeType.MERGE,
            mappedBy = "contracts")
    private Set<Practitioner> practitioners = new HashSet<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RuleConfiguration> ruleConfigurations = new HashSet<>();

    public LocalDate determineLocalDateFromDayOfSettlement() {
        if (this.getDayOfSettlement() == 31)
            return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return LocalDate.now().withDayOfMonth(this.getDayOfSettlement());
    }

}
