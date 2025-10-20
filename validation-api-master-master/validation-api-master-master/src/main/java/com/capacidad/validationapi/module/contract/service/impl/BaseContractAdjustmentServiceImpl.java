package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.contract.dto.ContractAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.contract.repository.BaseContractAdjustmentRepository;
import com.capacidad.validationapi.module.contract.service.BaseContractAdjustmentService;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.*;
import com.capacidad.validationapi.module.medicalauthorization.service.RestrictionTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public abstract class BaseContractAdjustmentServiceImpl<T extends ContractAdjustment, D extends ContractAdjustmentDTO> extends BaseServiceImpl<T, D, Long> implements BaseContractAdjustmentService<T, D> {

    private final BaseContractAdjustmentRepository<T, Long> repository;
    @Autowired
    private RestrictionTypeValidator restrictionTypeValidator;
    @Autowired
    private LocaleHandler localeHandler;
    @Autowired
    private RegionService regionService;

    public BaseContractAdjustmentServiceImpl(BaseContractAdjustmentRepository<T, Long> repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public void validate(ContractAdjustment contractAdjustment) throws ObjectNotValidException, ObjectNotFoundException {
        validateCityOrRegion(contractAdjustment);
        if (contractAdjustment.getRegion() != null)
            validateRegion(contractAdjustment);
        if (contractAdjustment.getCity() != null)
            validateCity(contractAdjustment);
    }

    private void validateCityOrRegion(ContractAdjustment contractAdjustment) throws ObjectNotValidException {
        if ((contractAdjustment.getRegion() != null) == (contractAdjustment.getCity() != null))
            throw new ObjectNotValidException("adjustment.regionOrCityRequired");
    }

    private void validateRegion(ContractAdjustment contractAdjustment) throws ObjectNotFoundException, ObjectNotValidException {
        Region region = this.getRegionService().findById(contractAdjustment.getRegion().getId());
        if (repository.existsByRegionOrCity(contractAdjustment.getNomenclator(),
                contractAdjustment.getContract(),
                region.getCities()))
            throw new ObjectAlreadyExistsException("adjustment.nomenclatorRegionAlreadyExists");
    }

    private void validateCity(ContractAdjustment contractAdjustment) throws ObjectNotValidException {
        if (repository.existsByRegionOrCity(contractAdjustment.getNomenclator(),
                contractAdjustment.getContract(),
                Collections.singleton(contractAdjustment.getCity())))
            throw new ObjectAlreadyExistsException("adjustment.nomenclatorCityAlreadyExists");
    }

    @Override
    public void validateUpdate(ContractAdjustment contractAdjustment) throws ObjectNotValidException, ObjectNotFoundException {
        validateCityOrRegion(contractAdjustment);
        if (contractAdjustment.getRegion() != null)
            validateRegionUpdate(contractAdjustment);
        if (contractAdjustment.getCity() != null)
            validateCityUpdate(contractAdjustment);
    }

    private void validateRegionUpdate(ContractAdjustment contractAdjustment) throws ObjectNotFoundException, ObjectNotValidException {
        Region region = this.getRegionService().findById(contractAdjustment.getRegion().getId());
        if (repository.existsByRegionOrCityAndIdIsNot(contractAdjustment.getNomenclator(),
                contractAdjustment.getContract(),
                region.getCities(),
                contractAdjustment.getId()))
            throw new ObjectAlreadyExistsException("adjustment.nomenclatorRegionAlreadyExists");
    }

    private void validateCityUpdate(ContractAdjustment contractAdjustment) throws ObjectNotValidException {
        if (repository.existsByRegionOrCityAndIdIsNot(contractAdjustment.getNomenclator(),
                contractAdjustment.getContract(),
                Collections.singleton(contractAdjustment.getCity()),
                contractAdjustment.getId()))
            throw new ObjectAlreadyExistsException("adjustment.nomenclatorCityAlreadyExists");
    }

    @Override
    public void applyRestriction(T contractAdjustment, MedicalAuthorizationItem medicalAuthorizationItem, RestrictionMessage restrictionMessage) {
        Restriction restriction = this.getRestrictionTypeValidator().buildRestriction(contractAdjustment.getRestrictionType(),
                FailureType.ADJUSTMENT,
                restrictionMessage);
        this.getRestrictionTypeValidator().applyRestriction(restriction, medicalAuthorizationItem);
    }

    protected RestrictionTypeValidator getRestrictionTypeValidator() {
        return this.restrictionTypeValidator;
    }

    protected RestrictionMessageExtra buildRestrictionExtraMessage(ContractAdjustment adjustment, LocalDateTime from) {
        return this.getRestrictionTypeValidator().buildRestrictionMessageExtra(RestrictionMessageExtraType.RAW_STRING,
                Collections.emptyList(),
                from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                getScopeName(adjustment.getScope()),
                adjustment.getContract().getName());
    }

    private String getScopeName(ContractAdjustmentScope scope) {
        return this.getLocaleHandler().getLocaleMessage(scope.name().toLowerCase(), LocaleContextHolder.getLocale()).orElse("");
    }

    protected LocaleHandler getLocaleHandler() {
        return this.localeHandler;
    }

    protected RegionService getRegionService() {
        return this.regionService;
    }

}
