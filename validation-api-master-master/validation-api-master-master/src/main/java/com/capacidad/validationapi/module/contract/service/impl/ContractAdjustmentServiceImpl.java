package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.model.ProjectionType;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.contract.dto.ContractAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.*;
import com.capacidad.validationapi.module.contract.projection.ContractAdjustmentProjection;
import com.capacidad.validationapi.module.contract.repository.ContractAdjustmentRepository;
import com.capacidad.validationapi.module.contract.service.ContractAdjustmentService;
import com.capacidad.validationapi.module.contract.service.MaximumAdjustmentService;
import com.capacidad.validationapi.module.contract.service.MonetaryAdjustmentService;
import com.capacidad.validationapi.module.contract.service.UsageRateAdjustmentService;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationCondition;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContractAdjustmentServiceImpl extends BaseContractAdjustmentServiceImpl<ContractAdjustment, ContractAdjustmentDTO> implements ContractAdjustmentService {

    private final MaximumAdjustmentService maximumAdjustmentService;
    private final UsageRateAdjustmentService usageRateAdjustmentService;
    private final ContractAdjustmentRepository contractAdjustmentRepository;
    private final MedicalAuthorizationItemService medicalAuthorizationItemService;
    private final MonetaryAdjustmentService monetaryAdjustmentService;
    private final RegionService regionService;

    @Autowired
    public ContractAdjustmentServiceImpl(ContractAdjustmentRepository repository,
                                         MaximumAdjustmentService maximumAdjustmentService,
                                         UsageRateAdjustmentService usageRateAdjustmentService,
                                         MedicalAuthorizationItemService medicalAuthorizationItemService,
                                         MonetaryAdjustmentService monetaryAdjustmentService,
                                         RegionService regionService) {
        super(repository);
        this.contractAdjustmentRepository = repository;
        this.maximumAdjustmentService = maximumAdjustmentService;
        this.usageRateAdjustmentService = usageRateAdjustmentService;
        this.medicalAuthorizationItemService = medicalAuthorizationItemService;
        this.monetaryAdjustmentService = monetaryAdjustmentService;
        this.regionService = regionService;
    }

    @Override
    public void applyContractAdjustments(MedicalAuthorizationItem medicalAuthorizationItem) {
        Contract contract = medicalAuthorizationItem.getContractItem().getContract();
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();
        boolean transitCondition = contract.getTransitCondition();
        boolean isTransit = false;
        if (transitCondition) {
            isTransit = validateTransitAdjustment(contract, beneficiary);
            if (isTransit) {
                AuthorizationCondition transitConditionType = this.getUtils().getGenericsEntityReference
                        (AuthorizationCondition.class, AuthorizationConditionReference.TRANSIT.getId());
                medicalAuthorization.setAuthorizationCondition(transitConditionType);
            }
        }
        if (!isTransit && !contract.getContractAdjustments().isEmpty())
            applyContractAdjustments(contract, medicalAuthorizationItem);
    }

    @Override
    public Page<ContractAdjustmentProjection> findAllContractAdjustments(Pageable pageable, String search) {
        PageRequest pageRequest = this.buildPageRequest(pageable);
        Page<? extends ContractAdjustmentProjection> result = buildGenericContractAdjustmentPage(search, pageRequest);
        return new PageImpl<>(new ArrayList<>(result.getContent()), result.getPageable(), result.getTotalElements());
    }

    private Page<ContractAdjustmentProjection> buildGenericContractAdjustmentPage(String search, Pageable pageable) {
        Optional<Specification<ContractAdjustment>> specification = this.getSpecificationBuilder().parseAndBuild(search);
        Page<ContractAdjustment> contractAdjustmentPage = specification.map(contractSpecification -> contractAdjustmentRepository.findAll(contractSpecification, pageable)).orElseGet(() -> contractAdjustmentRepository.findAll(pageable));
        List<ContractAdjustmentProjection> contractProjections = contractAdjustmentPage.getContent().stream()
                .map(this::buildContractAdjustmentProjection)
                .collect(Collectors.toUnmodifiableList());
        return new PageImpl<>(contractProjections, pageable, contractAdjustmentPage.getTotalElements());
    }

    private ContractAdjustmentProjection buildContractAdjustmentProjection(ContractAdjustment contractAdjustment) {
        Class<ContractAdjustmentProjection> projectionClazz = Utils.getEntityProjectionClass(contractAdjustment.getClass(), ProjectionType.ENTITY);
        return this.getProjectionFactory().createProjection(projectionClazz, contractAdjustment);
    }

    private void applyContractAdjustments(Contract contract, MedicalAuthorizationItem medicalAuthorizationItem) {
        Optional<ContractAdjustment> contractAdjustmentResult = findContractAdjustment(contract, medicalAuthorizationItem);
        if (contractAdjustmentResult.isPresent()) {
            ContractAdjustment contractAdjustment = contractAdjustmentResult.get();
            BigDecimal medicalAuthorizationValue = getItemConsumption(contractAdjustment, medicalAuthorizationItem);
            applyContractAdjustment(contractAdjustment, medicalAuthorizationValue, medicalAuthorizationItem);
        }
    }

    private BigDecimal getItemConsumption(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        return contractAdjustment instanceof MonetaryAdjustment ?
                medicalAuthorizationItemService
                        .sumNotTransitSubtotalsByContractAdjustmentAndPractitioner(contractAdjustment, medicalAuthorizationItem) :
                new BigDecimal(medicalAuthorizationItemService
                        .countNotTransitByContractAdjustmentAndPractitioner(contractAdjustment, medicalAuthorizationItem));
    }

    private boolean validateTransitAdjustment(Contract contract, Beneficiary beneficiary) {
        City beneficiaryCityLocation = beneficiary.getAddress().getCity();
        if (contract instanceof OrganizationContract) {
            OrganizationContract organizationContract = (OrganizationContract) contract;
            Optional<Region> region = Optional.ofNullable(organizationContract.getOrganization().getRegion());
            return region.map(value -> !regionService.cityBelongToRegion(value, beneficiaryCityLocation)).orElseGet(() -> !organizationContract.getOrganization().getAddress().getCity().getId().equals(beneficiaryCityLocation.getId()));
        }
        if (contract instanceof MedicalCenterContract) {
            MedicalCenterContract medicalCenterContract = (MedicalCenterContract) contract;
            return !medicalCenterContract.getMedicalCenter().getAddress().getCity().getId().equals(beneficiaryCityLocation.getId());
        }
        if (contract instanceof PractitionerContract) {
            PractitionerContract practitionerContract = (PractitionerContract) contract;
            return !practitionerContract.getPractitioner().getAddress().getCity().getId().equals(beneficiaryCityLocation.getId());
        }
        return false;
    }

    private Optional<ContractAdjustment> findContractAdjustment(Contract contract, MedicalAuthorizationItem medicalAuthorizationItem) {
        City city = medicalAuthorizationItem.getMedicalAuthorization().getCity();
        Nomenclator nomenclator = medicalAuthorizationItem.getNomenclator();
        return contractAdjustmentRepository
                .findByContractIdAndNomenclatorIdAndRegionCitiesId
                        (contract, nomenclator, Collections.singleton(city));
    }

    @Override
    public ContractAdjustment create(Contract contract, ContractAdjustmentDTO contractAdjustmentDTO) throws ObjectNotValidException, ObjectNotFoundException {
        //Cannot create instance of parent
        return null;
    }

    @Override
    public void applyContractAdjustment(ContractAdjustment contractAdjustment, BigDecimal medicalAuthorizationValue, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (contractAdjustment instanceof UsageRateAdjustment)
            usageRateAdjustmentService.applyContractAdjustment((UsageRateAdjustment) contractAdjustment, medicalAuthorizationValue, medicalAuthorizationItem);
        if (contractAdjustment instanceof MaximumAdjustment)
            maximumAdjustmentService.applyContractAdjustment((MaximumAdjustment) contractAdjustment, medicalAuthorizationValue, medicalAuthorizationItem);
        if (contractAdjustment instanceof MonetaryAdjustment)
            monetaryAdjustmentService.applyContractAdjustment((MonetaryAdjustment) contractAdjustment, medicalAuthorizationValue, medicalAuthorizationItem);
    }

}
