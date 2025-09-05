package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemRoleSpecificationBuilder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class MedicalAuthorizationItemRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<MedicalAuthorizationItem, Long> implements MedicalAuthorizationItemRoleSpecificationBuilder {

    private final ContractMediator contractMediator;
    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public MedicalAuthorizationItemRoleSpecificationBuilderImpl(ContractMediator contractMediator,
                                                                BeneficiaryFinder beneficiaryFinder) {
        this.contractMediator = contractMediator;
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForBeneficiaries() {
        return Optional.of((root, query, builder) -> {
            var beneficiaryJoin = root.join("medicalAuthorization")
                    .join("beneficiary");
            return builder.equal(beneficiaryJoin.get("familyId"), beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForPractitioner() {
        return Optional.of((root, query, builder) -> {
            var join = root.join("medicalAuthorization")
                    .join("practitioner");
            return builder.equal(join.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
        });
    }


    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForMedicalCenter() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var medAuth = root.join("medicalAuthorization");
            var resourceJoin = medAuth.join("medicalCenter");
            var joinContract = medAuth.join("contract");
            return builder.or(builder.equal(resourceJoin.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)),
                    joinContract.in(contracts));
        });
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForOrganizations() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var joinContract = root.join("medicalAuthorization")
                    .join("contract");
            var joinPractitionerReg = root.join("medicalAuthorization")
                    .join("practitioner")
                    .join("medicalRegistrations")
                    .join("organization");
            return builder.and(joinContract.in(contracts), builder.equal(joinPractitionerReg.get("resourceId"),
                    SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)));
        });
    }

}
