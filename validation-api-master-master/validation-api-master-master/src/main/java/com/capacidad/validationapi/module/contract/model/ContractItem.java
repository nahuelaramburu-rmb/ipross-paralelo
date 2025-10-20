package com.capacidad.validationapi.module.contract.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.practitioner.model.PractitionerCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "contract_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"contract_id", "nomenclator_id", "practitioner_category_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"contract_id", "module_id", "deleted", "deletion_token", "tenant_id"}),
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_item_seq", allocationSize = 1)
public class ContractItem extends BaseTenantEntity<Long> {

    @Audited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, updatable = false)
    private Contract contract;

    @Audited(withModifiedFlag = true)
    @Column
    private BigDecimal value;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_category_id")
    private PractitionerCategory practitionerCategory;

    @Audited(withModifiedFlag = true)
    @Column(columnDefinition = "boolean DEFAULT false", nullable = false)
    private Boolean refundable = false;

    @OneToMany(mappedBy = "contractItem")
    @BatchSize(size = 20)
    private Set<MedicalAuthorizationItem> medicalAuthorizationItems = new HashSet<>();

}
