package com.capacidad.validationapi.module.contract.config.security;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContractChecker {

    private final OrganizationContractService organizationContractService;
    private final MedicalCenterContractService medicalCenterContractService;
    private final PractitionerContractService practitionerContractService;
    private final ContractService contractService;
    private final ContractItemSpecialPriceService contractItemSpecialPriceService;

    @Autowired
    public ContractChecker(OrganizationContractService organizationContractService,
                           MedicalCenterContractService medicalCenterContractService,
                           PractitionerContractService practitionerContractService,
                           ContractService contractService,
                           ContractItemSpecialPriceService contractItemSpecialPriceService) {
        this.organizationContractService = organizationContractService;
        this.medicalCenterContractService = medicalCenterContractService;
        this.practitionerContractService = practitionerContractService;
        this.contractService = contractService;
        this.contractItemSpecialPriceService = contractItemSpecialPriceService;
    }

    public boolean hasAccessToContract(long contractId) {
        if (SecurityUtils.isPractitioner())
            return practitionerContractService.existByIdAndAuthPractitioner(contractId);
        if (SecurityUtils.isMedicalCenter())
            return medicalCenterContractService.existByIdAndAuthMedicalCenter(contractId);
        if (SecurityUtils.isOrganization())
            return organizationContractService.existByIdAndAuthOrganization(contractId);
        return SecurityUtils.isHighRankingAuthority();
    }

    public boolean hasAccessToContractItem(long contractItemId) throws ObjectNotFoundException {
        long parentId = contractService.getContractItemParentId(contractItemId);
        return hasAccessToContract(parentId);
    }

    public boolean hasAccessToContractItemSpecialPrice(long contractItemSpecialPriceId) throws ObjectNotFoundException {
        long parentId = contractItemSpecialPriceService.getContractItemSpecialPriceParentId(contractItemSpecialPriceId);
        return hasAccessToContractItem(parentId);
    }

    public boolean hasAccessToAdjustment(long adjustmentId) throws ObjectNotFoundException {
        long parentId = contractService.getAdjustmentContractId(adjustmentId);
        return hasAccessToContract(parentId);
    }

}
