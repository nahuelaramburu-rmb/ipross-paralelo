package com.capacidad.validationapi.module.batch.service.impl;

import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.batch.service.BatchRoleSpecificationBuilder;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BatchRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<Batch, Long> implements BatchRoleSpecificationBuilder {

    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public BatchRoleSpecificationBuilderImpl(BeneficiaryFinder beneficiaryFinder) {
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public Optional<Specification<Batch>> buildSpecificationForBeneficiaries() {
        return Optional.of((root, query, builder) -> {
            var beneficiaryJoin = root.join("beneficiary");
            return builder.equal(beneficiaryJoin.get("familyId"), beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
        });
    }


}
