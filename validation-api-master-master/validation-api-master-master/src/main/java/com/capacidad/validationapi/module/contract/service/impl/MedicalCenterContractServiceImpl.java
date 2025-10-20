package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.dto.MedicalCenterContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.MedicalCenterContract;
import com.capacidad.validationapi.module.contract.projection.MedicalCenterContractProjection;
import com.capacidad.validationapi.module.contract.repository.MedicalCenterContractRepository;
import com.capacidad.validationapi.module.contract.service.MedicalCenterContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class MedicalCenterContractServiceImpl extends BaseContractServiceImpl<MedicalCenterContract, MedicalCenterContractDTO> implements MedicalCenterContractService {

    private final MedicalCenterContractRepository medicalCenterContractRepository;

    @Autowired
    public MedicalCenterContractServiceImpl(MedicalCenterContractRepository repository) {
        super(repository);
        this.medicalCenterContractRepository = repository;
    }

    @Override
    public void validate(MedicalCenterContract medicalCenterContract) throws ObjectNotValidException {
        super.validate(medicalCenterContract);
        if (medicalCenterContractRepository
                .existsByMedicalCenterIdAndPeriod
                        (medicalCenterContract.getMedicalCenter().getId(), medicalCenterContract.getDateFrom(), medicalCenterContract.getDateTo()))
            throw new ObjectAlreadyExistsException("contract.medicalCenterContractAlreadyExists");
    }

    @Override
    public void validateUpdate(MedicalCenterContract medicalCenterContract) throws ObjectNotValidException {
        super.validate(medicalCenterContract);
        if (medicalCenterContractRepository
                .existsByIdNotAndMedicalCenterIdAndPeriod
                        (medicalCenterContract.getId(), medicalCenterContract.getMedicalCenter().getId(), medicalCenterContract.getDateFrom(), medicalCenterContract.getDateTo()))
            throw new ObjectAlreadyExistsException("contract.medicalCenterContractAlreadyExists");
    }

    @Override
    public Page<MedicalCenterContractProjection> findAllAuthMedicalCenterContracts(String search, Pageable pageable) {
        Optional<Specification<MedicalCenterContract>> optionalSpecification = this.getSpecificationBuilder().parseAndBuild(search);
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        Specification<MedicalCenterContract> appendedSpecification = ((root, query, builder) -> {
            var join = root.join("medicalCenter");
            return builder.equal(join.get("resourceId"), resourceId);
        });
        Specification<MedicalCenterContract> specification = optionalSpecification.isPresent() ? optionalSpecification.get().and(appendedSpecification)
                : appendedSpecification;
        return medicalCenterContractRepository.findAllProjectedBy(specification, MedicalCenterContractProjection.class, pageable);
    }

    @Override
    public Set<Contract> findAllAuthMedicalCenterContract() {
        return medicalCenterContractRepository.findAllByMedicalCenterResourceId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public boolean existByIdAndAuthMedicalCenter(long contractId) {
        return medicalCenterContractRepository.existsByIdAndMedicalCenterResourceId(contractId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }
}
