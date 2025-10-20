package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationRoleSpecificationBuilder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class MedicalAuthorizationRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<MedicalAuthorization, Long> implements MedicalAuthorizationRoleSpecificationBuilder {

    private final BeneficiaryFinder beneficiaryFinder;
    private final ContractMediator contractMediator;

    @Autowired
    public MedicalAuthorizationRoleSpecificationBuilderImpl(BeneficiaryFinder beneficiaryFinder,
                                                            ContractMediator contractMediator) {
        this.beneficiaryFinder = beneficiaryFinder;
        this.contractMediator = contractMediator;
    }

    @Override
    public Optional<Specification<MedicalAuthorization>> buildSpecificationForBeneficiaries() {
        return Optional.of((root, query, builder) -> {
            var beneficiaryJoin = root.join("beneficiary");
            return builder.equal(beneficiaryJoin.get("familyId"), beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<MedicalAuthorization>> buildSpecificationForPractitioner() {
        return Optional.of((root, query, builder) -> {
            var resourceJoin = root.join("practitioner");
            return builder.equal(resourceJoin.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<MedicalAuthorization>> buildSpecificationForMedicalCenter() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var resourceJoin = root.join("medicalCenter");
            var joinContract = root.join("contract");
            return builder.or(builder.equal(resourceJoin.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)),
                    joinContract.in(contracts));
        });
    }

    @Override
    public Optional<Specification<MedicalAuthorization>> buildSpecificationForOrganizations() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var joinContract = root.join("contract");
            var joinPractitioner = root.join("practitioner")
                    .join("medicalRegistrations")
                    .join("organization");
            return builder.and(joinContract.in(contracts), builder.equal(joinPractitioner.get("resourceId"),
                    SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)));
        });
    }
}
