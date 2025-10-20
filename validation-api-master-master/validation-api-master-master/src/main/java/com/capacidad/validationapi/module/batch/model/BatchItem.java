package com.capacidad.validationapi.module.batch.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "batch_item",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"nomenclator_id", "batch_id", "deleted", "deletion_token", "tenant_id"}),
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "batch_item_seq")
@Relation(collectionRelation = "batchItems")
public class BatchItem extends BaseTenantEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclator_id", nullable = false, updatable = false)
    private Nomenclator nomenclator;

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private Integer amount;

    @Audited(withModifiedFlag = true)
    @Enumerated(EnumType.STRING)
    @Column
    private Period period;

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "batch_item_medical_center",
            joinColumns = {@JoinColumn(name = "batch_item_id")},
            inverseJoinColumns = {@JoinColumn(name = "medical_center_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"batch_item_id", "medical_center_id"})
            }
    )
    private Set<MedicalCenter> medicalCenters = new HashSet<>();

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "batch_item_practitioner",
            joinColumns = {@JoinColumn(name = "batch_item_id")},
            inverseJoinColumns = {@JoinColumn(name = "practitioner_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"batch_item_id", "practitioner_id"})
            }
    )
    private Set<Practitioner> practitioners = new HashSet<>();

    /**
     * Workaround column always null for iOS app version compatibility
     * it should be removed in future releases
     */
    @Column
    private String practitioner;

    /**
     * Workaround column always null for iOS app version compatibility
     * it should be removed in future releases
     */
    @Column(name = "medical_center")
    private String medicalCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, updatable = false)
    private Batch batch;

}
