package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.dto.OrganizationContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.contract.projection.OrganizationContractProjection;
import com.capacidad.validationapi.module.contract.repository.OrganizationContractRepository;
import com.capacidad.validationapi.module.contract.service.OrganizationContractService;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.organization.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OrganizationContractServiceImpl extends BaseContractServiceImpl<OrganizationContract, OrganizationContractDTO> implements OrganizationContractService {

    private final OrganizationContractRepository organizationContractRepository;
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationContractServiceImpl(OrganizationContractRepository repository,
                                           OrganizationService organizationService) {
        super(repository);
        this.organizationContractRepository = repository;
        this.organizationService = organizationService;
    }

    @Override
    public void validate(OrganizationContract organizationContract) throws ObjectNotValidException {
        super.validate(organizationContract);
        if (organizationContractRepository
                .existsByOrganizationIdAndPeriod
                        (organizationContract.getOrganization().getId(), organizationContract.getDateFrom(), organizationContract.getDateTo()))
            throw new ObjectAlreadyExistsException("contract.organizationContractAlreadyExists");
    }

    @Override
    public void validateUpdate(OrganizationContract organizationContract) throws ObjectNotValidException {
        super.validate(organizationContract);
        if (organizationContractRepository
                .existsByIdNotAndOrganizationIdAndPeriod
                        (organizationContract.getId(), organizationContract.getOrganization().getId(), organizationContract.getDateFrom(), organizationContract.getDateTo()))
            throw new ObjectAlreadyExistsException("contract.organizationContractAlreadyExists");
    }

    @Override
    public Page<OrganizationContractProjection> findAllAuthOrganizationContracts(String search, Pageable pageable) {
        Optional<Specification<OrganizationContract>> optionalSpecification = this.getSpecificationBuilder().parseAndBuild(search);
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        Specification<OrganizationContract> appendedSpecification = ((root, query, builder) -> {
            var join = root.join("organization");
            return builder.equal(join.get("resourceId"), resourceId);
        });
        Specification<OrganizationContract> specification = optionalSpecification.isPresent() ? optionalSpecification.get().and(appendedSpecification)
                : appendedSpecification;
        return organizationContractRepository.findAllProjectedBy(specification, OrganizationContractProjection.class, pageable);
    }

    @Override
    public boolean existByIdAndAuthOrganization(long contractId) {
        return organizationContractRepository.existsByIdAndOrganizationResourceId(contractId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public Set<Contract> findAllAuthOrganizationAndRelatedContracts() throws ObjectNotFoundException {
        Organization organization = organizationService.getAuthOrganization();
        return organizationContractRepository.findAllByOrganizationOrOrganization(organization, organization.getRelatedOrganization());
    }

}
