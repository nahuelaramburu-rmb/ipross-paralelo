package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.dto.PractitionerContractDTO;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import com.capacidad.validationapi.module.contract.projection.PractitionerContractProjection;
import com.capacidad.validationapi.module.contract.repository.PractitionerContractRepository;
import com.capacidad.validationapi.module.contract.service.PractitionerContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PractitionerContractServiceImpl extends BaseContractServiceImpl<PractitionerContract, PractitionerContractDTO> implements PractitionerContractService {

    private final PractitionerContractRepository practitionerContractRepository;

    @Autowired
    public PractitionerContractServiceImpl(PractitionerContractRepository repository) {
        super(repository);
        this.practitionerContractRepository = repository;
    }

    @Override
    public void validate(PractitionerContract practitionerContract) throws ObjectNotValidException {
        super.validate(practitionerContract);
        if (practitionerContractRepository
                .existsByPractitionerIdAndPeriod
                        (practitionerContract.getPractitioner().getId(), practitionerContract.getDateFrom(), practitionerContract.getDateTo()))
            throw new ObjectAlreadyExistsException("contract.practitionerContractAlreadyExists");
    }

    @Override
    public void validateUpdate(PractitionerContract practitionerContract) throws ObjectNotValidException {
        super.validate(practitionerContract);
        if (practitionerContractRepository
                .existsByIdNotAndPractitionerIdAndPeriod
                        (practitionerContract.getId(), practitionerContract.getPractitioner().getId(), practitionerContract.getDateFrom(), practitionerContract.getDateTo()))
            throw new ObjectAlreadyExistsException("contract.practitionerContractAlreadyExists");
    }

    @Override
    public Page<PractitionerContractProjection> findAllAuthPractitionerContracts(String search, Pageable pageable) {
        Optional<Specification<PractitionerContract>> optionalSpecification = this.getSpecificationBuilder().parseAndBuild(search);
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        Specification<PractitionerContract> appendedSpecification = ((root, query, builder) -> {
            var join = root.join("practitioner");
            return builder.equal(join.get("resourceId"), resourceId);
        });
        Specification<PractitionerContract> specification = optionalSpecification.isPresent() ? optionalSpecification.get().and(appendedSpecification)
                : appendedSpecification;
        return practitionerContractRepository.findAllProjectedBy(specification, PractitionerContractProjection.class, pageable);
    }

    @Override
    public boolean existByIdAndAuthPractitioner(long contractId) {
        return practitionerContractRepository.existsByIdAndPractitionerResourceId(contractId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }
}
