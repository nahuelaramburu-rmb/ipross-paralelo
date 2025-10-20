package com.capacidad.validationapi.module.contract.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionType;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "contract_adjustment")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_adjustment_seq", allocationSize = 1)
@Relation(collectionRelation = "adjustments")
public class ContractAdjustment extends BaseTenantEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false)
    private Nomenclator nomenclator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Audited(withModifiedFlag = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false)
    private Period period;

    @Audited(withModifiedFlag = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private ContractAdjustmentScope scope;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restriction_type_id")
    private RestrictionType restrictionType;

    @Column(name = "dtype", insertable = false, updatable = false)
    private String type;

    public boolean isContractScope() {
        return this.getScope().equals(ContractAdjustmentScope.CONTRACT);
    }

    public boolean isPractitionerScope() {
        return this.getScope().equals(ContractAdjustmentScope.PRACTITIONER);
    }

}
