package com.capacidad.validationapi.module.premedicalauthorization.service.impl;

import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationRoleSpecificationBuilder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PreMedicalAuthorizationRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<PreMedicalAuthorization, Long> implements PreMedicalAuthorizationRoleSpecificationBuilder {

    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public PreMedicalAuthorizationRoleSpecificationBuilderImpl(BeneficiaryFinder beneficiaryFinder) {
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public Optional<Specification<PreMedicalAuthorization>> buildSpecificationForBeneficiaries() {
        return Optional.of((root, query, builder) -> {
            var beneficiaryJoin = root.join("beneficiary");
            return builder.equal(beneficiaryJoin.get("familyId"), beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
        });
    }

}
