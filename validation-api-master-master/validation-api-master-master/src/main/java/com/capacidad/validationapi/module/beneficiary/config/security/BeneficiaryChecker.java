package com.capacidad.validationapi.module.beneficiary.config.security;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryInsurancePlanService;
import com.capacidad.validationapi.module.beneficiary.service.ExpirationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryChecker {

    private final BeneficiaryFinder beneficiaryFinder;
    private final ExpirationService expirationService;
    private final BeneficiaryInsurancePlanService beneficiaryInsurancePlanService;

    @Autowired
    public BeneficiaryChecker(BeneficiaryFinder beneficiaryFinder,
                              ExpirationService expirationService,
                              BeneficiaryInsurancePlanService beneficiaryInsurancePlanService) {
        this.beneficiaryFinder = beneficiaryFinder;
        this.expirationService = expirationService;
        this.beneficiaryInsurancePlanService = beneficiaryInsurancePlanService;
    }

    public boolean canGetAll() {
        return !SecurityUtils.isBeneficiary();
    }

    public boolean hasAccessToBeneficiary(long beneficiaryId) {
        if (SecurityUtils.isBeneficiary())
            return beneficiaryFinder.correspondsToAuthentication(beneficiaryId);
        return true;
    }

    public boolean hasAccessToExpiration(long expirationId) throws ObjectNotFoundException {
        long beneficiaryId = expirationService.findExpirationAndGetBeneficiaryId(expirationId);
        return hasAccessToBeneficiary(beneficiaryId);
    }

    public boolean hasAccessToBeneficiaryInsurancePlan(long beneficiaryInsurancePlanId) throws ObjectNotFoundException {
        long beneficiaryId = beneficiaryInsurancePlanService.findBeneficiaryInsurancePlanAndGetBeneficiaryId(beneficiaryInsurancePlanId);
        return hasAccessToBeneficiary(beneficiaryId);
    }

}
