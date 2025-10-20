package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.contract.dto.MaximumAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.MaximumAdjustment;
import com.capacidad.validationapi.module.contract.repository.MaximumAdjustmentRepository;
import com.capacidad.validationapi.module.contract.service.MaximumAdjustmentService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionMessage;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Log4j2
@Service
public class MaximumAdjustmentServiceImpl extends BaseContractAdjustmentServiceImpl<MaximumAdjustment, MaximumAdjustmentDTO> implements MaximumAdjustmentService {

    private final MaximumAdjustmentRepository maximumAdjustmentRepository;

    @Autowired
    public MaximumAdjustmentServiceImpl(MaximumAdjustmentRepository repository) {
        super(repository);
        this.maximumAdjustmentRepository = repository;
    }

    @Override
    public MaximumAdjustment create(Contract contract, MaximumAdjustmentDTO maximumAdjustmentDTO) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({}), {}({})", contract.getClass(), contract, maximumAdjustmentDTO.getClass(), maximumAdjustmentDTO);
        MaximumAdjustment objectToPersist = this.mapDtoToInput(maximumAdjustmentDTO);
        objectToPersist.setContract(contract);
        this.validate(objectToPersist);
        MaximumAdjustment objectResponse = maximumAdjustmentRepository.save(objectToPersist);
        log.info("create - void: {}({}), {}({})", contract.getClass(), contract, objectResponse.getClass(), objectResponse);
        return objectResponse;
    }

    @Override
    public void applyContractAdjustment(MaximumAdjustment maximumAdjustment, BigDecimal medicalAuthorizationValue, MedicalAuthorizationItem medicalAuthorizationItem) {
        BigDecimal currentTransactionValue = medicalAuthorizationValue.add(new BigDecimal(medicalAuthorizationItem.getQuantity()));
        if (currentTransactionValue.compareTo(new BigDecimal(maximumAdjustment.getThreshold())) > 0) {
            LocalDateTime from = DateUtils.resolvePeriodDateFrom(maximumAdjustment.getPeriod());
            RestrictionMessage restrictionMessage = this.getRestrictionTypeValidator().buildRestrictionMessage(maximumAdjustment.getClass().getSimpleName().toLowerCase(),
                    maximumAdjustment.getThreshold().toString(),
                    currentTransactionValue.toString(),
                    this.buildRestrictionExtraMessage(maximumAdjustment, from));
            medicalAuthorizationItem.setAuthorizationCondition(AuthorizationConditionReference.MAXIMUM_EXCEEDED.getInstance());
            this.applyRestriction(maximumAdjustment, medicalAuthorizationItem, restrictionMessage);
        }
    }

}
