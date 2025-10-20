package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemExportRoleSpecificationBuilder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class MedicalAuthorizationItemExportRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<MedicalAuthorizationItem, Long> implements MedicalAuthorizationItemExportRoleSpecificationBuilder {

    private final BeneficiaryFinder beneficiaryFinder;
    private final ContractMediator contractMediator;

    @Autowired
    public MedicalAuthorizationItemExportRoleSpecificationBuilderImpl(BeneficiaryFinder beneficiaryFinder,
                                                                      ContractMediator contractMediator) {
        this.beneficiaryFinder = beneficiaryFinder;
        this.contractMediator = contractMediator;
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForBeneficiaries() {
        return Optional.of((root, query, builder) -> {
            var mainRoot = root.join("medicalAuthorization");
            var beneficiaryJoin = mainRoot.join("beneficiary");
            return builder.equal(beneficiaryJoin.get("familyId"), beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForPractitioner() {
        return buildSpecificationForPractitionerMedicalCenter("practitioner");
    }

    private Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForPractitionerMedicalCenter(String resource) {
        return Optional.of((root, query, builder) -> {
            var mainRoot = root.join("medicalAuthorization");
            var practOrMedCenJoin = mainRoot.join(resource);
            return builder.equal(practOrMedCenJoin.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForMedicalCenter() {
        return buildSpecificationForPractitionerMedicalCenter("medicalCenter");
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> buildSpecificationForOrganizations() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var mainRoot = root.join("medicalAuthorization");
            var joinContract = mainRoot.join("contract");
            var joinPractitioner = mainRoot.join("practitioner")
                    .join("medicalRegistrations")
                    .join("organization");
            return builder.and(joinContract.in(contracts), builder.equal(joinPractitioner.get("resourceId"),
                    SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)));
        });
    }
}
