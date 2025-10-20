package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import com.capacidad.validationapi.module.medicalauthorization.repository.MedicalAuthorizationItemRepository;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_APPROVED;

@Component
public class MedicalAuthorizationItemContractAdjustmentQueryResolver {

    private final MedicalAuthorizationItemRepository medicalAuthorizationItemRepository;

    @Autowired
    public MedicalAuthorizationItemContractAdjustmentQueryResolver(MedicalAuthorizationItemRepository medicalAuthorizationItemRepository) {
        this.medicalAuthorizationItemRepository = medicalAuthorizationItemRepository;
    }

    public long countNotTransitByCityAndContractScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndNomenclatorAndCityIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getCity().getId(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        medicalAuthorizationItem.getId());
        return getQuantitySum(items);
    }

    public long countNotTransitByCityAndPractitionerScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        Practitioner practitioner = medicalAuthorizationItem.getMedicalAuthorization().getPractitioner();
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndPractitionerAndNomenclatorAndCityIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getCity().getId(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        practitioner.getId(),
                        medicalAuthorizationItem.getId());
        return getQuantitySum(items);
    }

    public long countNotTransitByRegionAndContractScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndNomenclatorAndCitiesIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getRegion().getCityIds(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        medicalAuthorizationItem.getId());
        return getQuantitySum(items);
    }

    public long countNotTransitByRegionAndPractitionerScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        Practitioner practitioner = medicalAuthorizationItem.getMedicalAuthorization().getPractitioner();
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndPractitionerAndNomenclatorAndCitiesIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getRegion().getCityIds(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        practitioner.getId(),
                        medicalAuthorizationItem.getId());
        return getQuantitySum(items);
    }

    public BigDecimal sumNotTransitByCityAndContractScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndNomenclatorAndCityIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getCity().getId(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        medicalAuthorizationItem.getId());
        return getSubtotalSum(items);
    }

    public BigDecimal sumNotTransitByCityAndPractitionerScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        Practitioner practitioner = medicalAuthorizationItem.getMedicalAuthorization().getPractitioner();
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndPractitionerAndNomenclatorAndCityIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getCity().getId(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        practitioner.getId(),
                        medicalAuthorizationItem.getId());
        return getSubtotalSum(items);
    }

    public BigDecimal sumNotTransitByRegionAndContractScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndNomenclatorAndCitiesIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getRegion().getCityIds(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        medicalAuthorizationItem.getId());
        return getSubtotalSum(items);
    }

    public BigDecimal sumNotTransitByRegionAndPractitionerScope(ContractAdjustment contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem) {
        Practitioner practitioner = medicalAuthorizationItem.getMedicalAuthorization().getPractitioner();
        LocalDateTime dateTimeFrom = DateUtils.resolvePeriodDateFrom(contractAdjustment.getPeriod());
        List<MedicalAuthorizationItem> items = medicalAuthorizationItemRepository
                .findAllByContractAndPractitionerAndNomenclatorAndCitiesIgnoringAuthorizationConditionInPeriod(
                        contractAdjustment.getContract().getId(),
                        contractAdjustment.getNomenclator().getId(),
                        AuthorizationConditionReference.TRANSIT.getId(),
                        contractAdjustment.getRegion().getCityIds(),
                        dateTimeFrom,
                        LocalDateTime.now(),
                        VALIDATION_APPROVED.getId(),
                        practitioner.getId(),
                        medicalAuthorizationItem.getId());
        return getSubtotalSum(items);
    }

    private long getQuantitySum(List<MedicalAuthorizationItem> items) {
        return items.stream()
                .mapToInt(MedicalAuthorizationItem::getQuantity)
                .sum();
    }

    private BigDecimal getSubtotalSum(List<MedicalAuthorizationItem> items) {
        return items.stream()
                .map(MedicalAuthorizationItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
