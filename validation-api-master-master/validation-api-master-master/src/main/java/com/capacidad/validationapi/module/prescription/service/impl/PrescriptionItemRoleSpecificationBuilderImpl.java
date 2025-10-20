package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.service.PrescriptionItemRoleSpecificationBuilder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PrescriptionItemRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<PrescriptionItem, Long> implements PrescriptionItemRoleSpecificationBuilder {

    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public PrescriptionItemRoleSpecificationBuilderImpl(BeneficiaryFinder beneficiaryFinder) {
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public Optional<Specification<PrescriptionItem>> buildSpecificationForBeneficiaries() {
        return Optional.of((root, query, builder) -> {
            var join = root.join("prescription").join("beneficiary");
            return builder.equal(join.get("familyId"), beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<PrescriptionItem>> buildSpecificationForPractitioner() {
        return buildSpecificationForPractitionerOrMedicalCenter("practitioner");
    }

    private Optional<Specification<PrescriptionItem>> buildSpecificationForPractitionerOrMedicalCenter(String entity) {
        return Optional.of((root, query, builder) -> {
            var join = root.join("prescription").join(entity);
            return builder.equal(join.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<PrescriptionItem>> buildSpecificationForMedicalCenter() {
        return buildSpecificationForPractitionerOrMedicalCenter("medicalCenter");
    }

}
