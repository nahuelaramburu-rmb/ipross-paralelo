package com.capacidad.validationapi.module.insuranceplan.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryInsurancePlan;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "insurance_plan",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "insurance_plan_type_id", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "insurance_plan_seq", allocationSize = 1)
@Relation(collectionRelation = "insurancePlans")
public class InsurancePlan extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_plan_type_id", nullable = false)
    private InsurancePlanType insurancePlanType;

    @Column(nullable = false)
    private Integer priority;

    @OneToMany(mappedBy = "insurancePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MedicalCoverage> medicalCoverages = new HashSet<>();

    @OneToMany(mappedBy = "insurancePlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<BeneficiaryInsurancePlan> beneficiaryInsurancePlans = new ArrayList<>();

    @Override
    public void associateChildObjects() {
        this.medicalCoverages.forEach(c -> c.setInsurancePlan(this));
    }

}
