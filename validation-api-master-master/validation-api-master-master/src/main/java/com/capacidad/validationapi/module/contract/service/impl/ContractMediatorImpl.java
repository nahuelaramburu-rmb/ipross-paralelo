package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.projection.MedicalCenterContractProjection;
import com.capacidad.validationapi.module.contract.projection.OrganizationContractProjection;
import com.capacidad.validationapi.module.contract.projection.PractitionerContractProjection;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.contract.service.MedicalCenterContractService;
import com.capacidad.validationapi.module.contract.service.OrganizationContractService;
import com.capacidad.validationapi.module.contract.service.PractitionerContractService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Log4j2
@Component
public class ContractMediatorImpl implements ContractMediator {

    private final OrganizationContractService organizationContractService;
    private final MedicalCenterContractService medicalCenterContractService;
    private final PractitionerContractService practitionerContractService;

    @Autowired
    public ContractMediatorImpl(OrganizationContractService organizationContractService,
                                MedicalCenterContractService medicalCenterContractService,
                                PractitionerContractService practitionerContractService) {
        this.organizationContractService = organizationContractService;
        this.medicalCenterContractService = medicalCenterContractService;
        this.practitionerContractService = practitionerContractService;
    }

    @Override
    public Page<OrganizationContractProjection> findAllAuthOrganizationContracts(String search, Pageable pageable) {
        return organizationContractService.findAllAuthOrganizationContracts(search, pageable);
    }

    @Override
    public Page<MedicalCenterContractProjection> findAllAuthMedicalCenterContracts(String search, Pageable pageable) {
        return medicalCenterContractService.findAllAuthMedicalCenterContracts(search, pageable);
    }

    @Override
    public Set<Contract> findAllAuthMedicalCenterContracts() {
        return medicalCenterContractService.findAllAuthMedicalCenterContract();
    }

    @Override
    public Set<Contract> findAllAuthOrganizationAndRelatedContracts() throws ObjectNotFoundException {
        return organizationContractService.findAllAuthOrganizationAndRelatedContracts();
    }

    @Override
    public Set<Contract> findAllAuthContracts() {
        try {
            return SecurityUtils.isMedicalCenter() ? findAllAuthMedicalCenterContracts() :
                    findAllAuthOrganizationAndRelatedContracts();
        } catch (ObjectNotFoundException e) {
            log.error("Contracts not found: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public Page<PractitionerContractProjection> findAllAuthPractitionerContracts(String search, Pageable pageable) {
        return practitionerContractService.findAllAuthPractitionerContracts(search, pageable);
    }

}
