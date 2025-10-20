package com.capacidad.validationapi.module.nomenclator.model;

import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "nomenclator",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"nomenclator_code", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"medical_practice_id", "medical_practice_area_id", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "nomenclator_seq", allocationSize = 1)
@Relation(collectionRelation = "nomenclators")
public class Nomenclator extends BaseTenantEntity<Long> {

    @Column(name = "nomenclator_code", nullable = false)
    private String nomenclatorCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_practice_id", nullable = false)
    private MedicalPractice medicalPractice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_practice_area_id", nullable = false)
    private MedicalPracticeArea medicalPracticeArea;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_practice_type_id", nullable = false)
    private MedicalPracticeType medicalPracticeType;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "nomenclator_config_id", nullable = false)
    private NomenclatorConfig nomenclatorConfig;

    @ManyToMany(mappedBy = "nomenclators", cascade = CascadeType.MERGE)
    private Set<NomenclatorGroup> nomenclatorGroups = new HashSet<>();

    @ManyToMany(mappedBy = "nomenclators")
    private Set<AuditTray> auditTrays = new HashSet<>();

    @OneToMany(mappedBy = "nomenclator")
    @BatchSize(size = 20)
    private Set<ContractAdjustment> contractAdjustments = new HashSet<>();

    @OneToMany(mappedBy = "nomenclator")
    @BatchSize(size = 20)
    private Set<BatchItem> batchItems = new HashSet<>();

    @OneToMany(mappedBy = "nomenclator")
    @BatchSize(size = 20)
    private Set<FixedContractItem> fixedContractItems = new HashSet<>();

    @OneToMany(mappedBy = "nomenclator")
    @BatchSize(size = 20)
    private Set<MedicalCoverageItem> medicalCoverageItems = new HashSet<>();

}
