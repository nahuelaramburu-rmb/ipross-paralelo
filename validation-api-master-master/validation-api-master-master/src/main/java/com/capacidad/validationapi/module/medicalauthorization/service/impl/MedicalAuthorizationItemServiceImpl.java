package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationItemDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.repository.MedicalAuthorizationItemRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemRoleSpecificationBuilder;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;

@Log4j2
@Service
@Transactional
public class MedicalAuthorizationItemServiceImpl extends BaseServiceImpl<MedicalAuthorizationItem, MedicalAuthorizationItemDTO, Long> implements MedicalAuthorizationItemService {

    private final MedicalAuthorizationItemRepository medicalAuthorizationItemRepository;
    private final MedicalAuthorizationItemRoleSpecificationBuilder roleSpecificationBuilder;
    private final MedicalAuthorizationItemContractAdjustmentQueryResolver contractAdjustmentQueryResolver;

    @Autowired
    public MedicalAuthorizationItemServiceImpl(MedicalAuthorizationItemRepository repository,
                                               MedicalAuthorizationItemRoleSpecificationBuilder roleSpecificationBuilder,
                                               MedicalAuthorizationItemContractAdjustmentQueryResolver contractAdjustmentQueryResolver) {
        super(repository);
        this.medicalAuthorizationItemRepository = repository;
        this.roleSpecificationBuilder = roleSpecificationBuilder;
        this.contractAdjustmentQueryResolver = contractAdjustmentQueryResolver;
    }

    @Override
    public Set<MedicalAuthorizationItem> findNotSettledItemsByPractitionerContractAndCollectionOfIds(Practitioner practitioner, Contract contract, Collection<Long> medicalAuthorizationItemIds) {
        return medicalAuthorizationItemRepository
                .findAllByIdInAndSettledIsFalseAndMedicalAuthorizationPractitionerIdAndMedicalAuthorizationContractId
                        (medicalAuthorizationItemIds, practitioner.getId(), contract.getId());
    }

    @Override
    public List<MedicalAuthorizationItem> saveAll(Collection<MedicalAuthorizationItem> medicalAuthorizationItems) {
        log.info("saveAll - args: Collection<MedicalAuthorizationItem> ({}), size; {}",
                medicalAuthorizationItems, medicalAuthorizationItems.size());
        return medicalAuthorizationItemRepository.saveAll(medicalAuthorizationItems);
    }

    @Override
    public long countNotTransitByContractAdjustmentAndPractitioner(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (contractAdjustment.getRegion() == null) {
            if (contractAdjustment.isContractScope())
                return contractAdjustmentQueryResolver
                        .countNotTransitByCityAndContractScope(contractAdjustment, medicalAuthorizationItem);
            return contractAdjustmentQueryResolver
                    .countNotTransitByCityAndPractitionerScope(contractAdjustment, medicalAuthorizationItem);
        }
        if (contractAdjustment.isPractitionerScope())
            return contractAdjustmentQueryResolver
                    .countNotTransitByRegionAndPractitionerScope(contractAdjustment, medicalAuthorizationItem);
        return contractAdjustmentQueryResolver
                .countNotTransitByRegionAndContractScope(contractAdjustment, medicalAuthorizationItem);
    }

    @Override
    public BigDecimal sumNotTransitSubtotalsByContractAdjustmentAndPractitioner(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        if (contractAdjustment.getRegion() == null) {
            if (contractAdjustment.isContractScope())
                return contractAdjustmentQueryResolver
                        .sumNotTransitByCityAndContractScope(contractAdjustment, medicalAuthorizationItem);
            return contractAdjustmentQueryResolver
                    .sumNotTransitByCityAndPractitionerScope(contractAdjustment, medicalAuthorizationItem);
        }
        if (contractAdjustment.isPractitionerScope())
            return contractAdjustmentQueryResolver
                    .sumNotTransitByRegionAndPractitionerScope(contractAdjustment, medicalAuthorizationItem);
        return contractAdjustmentQueryResolver
                .sumNotTransitByRegionAndContractScope(contractAdjustment, medicalAuthorizationItem);
    }

    @Override
    public long countAllByBatchItemInPeriod(BatchItem batchItem) {
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(batchItem.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByBatchItemAndStatusInPeriod(batchItem.getNomenclator().getId(),
                        batchItem.getBatch().getBeneficiary().getId(),
                        VALIDATION_APPROVED.getId(),
                        dateTimeFrom,
                        LocalDateTime.now());
        return getQuantitySum(items);
    }

    private long getQuantitySum(List<MedicalAuthorizationItem> items) {
        return items.stream()
                .mapToInt(MedicalAuthorizationItem::getQuantity)
                .sum();
    }

    @Override
    public Optional<Specification<MedicalAuthorizationItem>> appendCustomSpecification() {
        return roleSpecificationBuilder.buildSpecification();
    }

    @Override
    public MedicalAuthorizationItem save(MedicalAuthorizationItem medicalAuthorizationItem) {
        return medicalAuthorizationItemRepository.save(medicalAuthorizationItem);
    }

    @Override
    public List<MedicalAuthorizationItem> getBeneficiaryPractitionerAuthorizationItemsInPeriod(MedicalAuthorizationItem medicalAuthorizationItem, LocalDateTime from) {
        MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
        Beneficiary beneficiary = medicalAuthorization.getBeneficiary();
        Practitioner practitioner = medicalAuthorization.getPractitioner();
        Nomenclator nomenclator = medicalAuthorizationItem.getNomenclator();
        return medicalAuthorizationItemRepository
                .findAllByNomenclatorBeneficiaryPractitionerStatusIdAndPeriod
                        (nomenclator, beneficiary, practitioner, from, VALIDATION_APPROVED.getId(), medicalAuthorizationItem.getId());
    }

    @Override
    public List<MedicalAuthorizationItem> getBeneficiaryAuthorizationItemAmountInPeriod(MedicalAuthorizationItem medicalAuthorizationItem, LocalDateTime from) {
        Beneficiary beneficiary = medicalAuthorizationItem.getMedicalAuthorization().getBeneficiary();
        Nomenclator nomenclator = medicalAuthorizationItem.getNomenclator();
        return medicalAuthorizationItemRepository
                .findAllByNomenclatorIdAndMedicalAuthorizationBeneficiaryIdAndCreatedAtBetweenAndStatusIdAndIdIsNot
                        (nomenclator.getId(), beneficiary.getId(), from, LocalDateTime.now(), VALIDATION_APPROVED.getId(), medicalAuthorizationItem.getId());
    }

    @Override
    public int countBeneficiaryAuthorizationItemAmount(MedicalAuthorizationItem medicalAuthorizationItem, Status status) {
        Beneficiary beneficiary = medicalAuthorizationItem.getMedicalAuthorization().getBeneficiary();
        Nomenclator nomenclator = medicalAuthorizationItem.getNomenclator();
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByMedicalAuthorizationBeneficiaryIdAndNomenclatorIdAndStatusIdAndIdIsNot
                        (beneficiary.getId(), nomenclator.getId(), status.getId(), medicalAuthorizationItem.getId());
        return items.size();
    }

    @Override
    public int countBeneficiaryFreeAuthorizationItemAmountInPeriod(MedicalAuthorizationItem medicalAuthorizationItem, long days) {
        Beneficiary beneficiary = medicalAuthorizationItem.getMedicalAuthorization().getBeneficiary();
        Nomenclator nomenclator = medicalAuthorizationItem.getNomenclator();
        InsurancePlan plan = medicalAuthorizationItem.getMedicalCoverageItem().getMedicalCoverage().getInsurancePlan();
        LocalDateTime from = LocalDate.now().minusDays(days - 1).atTime(0, 0, 0, 0);
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllFreeMedAuthItemsByBeneficiaryNomenclatorStatusPlanBetweenDates
                        (beneficiary.getId(), nomenclator.getId(), VALIDATION_APPROVED.getId(), plan.getId(),
                                from, LocalDateTime.now());
        return (int) getQuantitySum(items);

    }

}
