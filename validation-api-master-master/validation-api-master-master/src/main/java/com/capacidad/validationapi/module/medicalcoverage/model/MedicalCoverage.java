package com.capacidad.validationapi.module.medicalcoverage.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.nomenclator.model.MedicalPracticeArea;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medical_coverage")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "medical_coverage_seq", allocationSize = 1)
@Relation(collectionRelation = "medicalCoverages")
public class MedicalCoverage extends BaseTenantEntity<Long> {

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private String name;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_plan_id", nullable = false)
    private InsurancePlan insurancePlan;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_practice_area_id", nullable = false)
    private MedicalPracticeArea medicalPracticeArea;

    @OneToMany(mappedBy = "medicalCoverage", cascade = CascadeType.ALL,
            orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<MedicalCoverageItem> medicalCoverageItems = new HashSet<>();

    @Override
    public void associateChildObjects() {
        this.medicalCoverageItems.forEach(i -> i.setMedicalCoverage(this));
    }

}
