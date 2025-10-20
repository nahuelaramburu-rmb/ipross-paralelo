package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.model.ProjectionType;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.contract.dto.ContractDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.projection.ContractProjection;
import com.capacidad.validationapi.module.contract.repository.ContractRepository;
import com.capacidad.validationapi.module.contract.service.ContractItemService;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.contract.service.ContractService;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class ContractServiceImpl extends BaseContractServiceImpl<Contract, ContractDTO> implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractItemService contractItemService;
    private final ContractMediator contractMediator;

    @Autowired
    public ContractServiceImpl(ContractRepository repository,
                               ContractItemService contractItemService,
                               ContractMediator contractMediator) {
        super(repository);
        this.contractRepository = repository;
        this.contractItemService = contractItemService;
        this.contractMediator = contractMediator;
    }

    @Override
    public void validate(Contract contract) throws ObjectNotValidException {
        super.validate(contract);
    }

    @Override
    public Page<ContractProjection> findAllContracts(Pageable pageable, String search) {
        PageRequest pageRequest = this.buildPageRequest(pageable);
        Page<? extends ContractProjection> result = buildContractPage(search, pageRequest);
        return new PageImpl<>(new ArrayList<>(result.getContent()), result.getPageable(), result.getTotalElements());
    }

    @Override
    public <P extends BaseProjection<Long>> EntityModel<P> findProjectedById(Long objectId) throws ObjectNotFoundException {
        Contract contract = this.findById(objectId);
        return Utils.projectionToResourceMapping(contract.getClass(), buildContractProjection(contract));
    }

    private Page<? extends ContractProjection> buildContractPage(String search, Pageable pageable) {
        if (SecurityUtils.isPractitioner())
            return contractMediator.findAllAuthPractitionerContracts(search, pageable);
        if (SecurityUtils.isMedicalCenter())
            return contractMediator.findAllAuthMedicalCenterContracts(search, pageable);
        if (SecurityUtils.isOrganization())
            return contractMediator.findAllAuthOrganizationContracts(search, pageable);
        return buildGenericContractPage(search, pageable);
    }

    private Page<ContractProjection> buildGenericContractPage(String search, Pageable pageable) {
        Optional<Specification<Contract>> specification = this.getSpecificationBuilder().parseAndBuild(search);
        Page<Contract> contractPage = specification.map(contractSpecification -> contractRepository.findAll(contractSpecification, pageable)).orElseGet(() -> contractRepository.findAll(pageable));
        List<ContractProjection> contractProjections = contractPage.getContent().stream()
                .map(this::buildContractProjection)
                .collect(Collectors.toUnmodifiableList());
        return new PageImpl<>(contractProjections, pageable, contractPage.getTotalElements());
    }

    private ContractProjection buildContractProjection(Contract contract) {
        Class<ContractProjection> projectionClazz = Utils.getEntityProjectionClass(contract.getClass(), ProjectionType.ENTITY);
        return this.getProjectionFactory().createProjection(projectionClazz, contract);
    }

    @Override
    public void calculateAuthorizationItemPrice(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        Contract contract = medicalAuthorizationItem.getMedicalAuthorization().getContract();
        ContractItem contractItem = applyContractCalculationStrategy(contract, medicalAuthorizationItem);
        medicalAuthorizationItem.setContractItem(contractItem);
    }

    @Override
    public long getContractItemParentId(long contractItemId) throws ObjectNotFoundException {
        BaseProjection<Long> result = contractRepository.findByContractItemsId(contractItemId)
                .orElseThrow(() -> new ObjectNotFoundException("contractItem.notFound", String.valueOf(contractItemId)));
        return result.getId();
    }

    @Override
    public long getAdjustmentContractId(long adjustmentId) throws ObjectNotFoundException {
        BaseProjection<Long> result = contractRepository.findByContractAdjustmentsId(adjustmentId)
                .orElseThrow(() -> new ObjectNotFoundException("adjustment.notFound", String.valueOf(adjustmentId)));
        return result.getId();
    }

    @Override
    public Set<IdAndNameOnlyProjection> getPractitionerContracts(long practitionerId) {
        return contractRepository.findAllByPractitionersId(practitionerId);
    }

    @Override
    public Set<ContractProjection> findContractsContaining(String nameOrCode) {
        return contractRepository
                .findAllByNameContainingIgnoreCaseOrContractCodeContainingIgnoreCase
                        (nameOrCode, nameOrCode);
    }

    private ContractItem applyContractCalculationStrategy(Contract contract, MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException {
        return contractItemService.calculateAuthorizationItemPrice(contract, medicalAuthorizationItem);
    }

}
