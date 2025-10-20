package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.contract.dto.MonetaryAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.MonetaryAdjustment;
import com.capacidad.validationapi.module.contract.repository.MonetaryAdjustmentRepository;
import com.capacidad.validationapi.module.contract.service.MonetaryAdjustmentService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionMessage;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Log4j2
@Service
public class MonetaryAdjustmentServiceImpl extends BaseContractAdjustmentServiceImpl<MonetaryAdjustment, MonetaryAdjustmentDTO> implements MonetaryAdjustmentService {

    private final MonetaryAdjustmentRepository monetaryAdjustmentRepository;

    @Autowired
    public MonetaryAdjustmentServiceImpl(MonetaryAdjustmentRepository repository) {
        super(repository);
        this.monetaryAdjustmentRepository = repository;
    }

    @Override
    public MonetaryAdjustment create(Contract contract, MonetaryAdjustmentDTO monetaryAdjustmentDTO) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({}), {}({})", contract.getClass(), contract, monetaryAdjustmentDTO.getClass(), monetaryAdjustmentDTO);
        MonetaryAdjustment objectToPersist = this.mapDtoToInput(monetaryAdjustmentDTO);
        objectToPersist.setContract(contract);
        this.validate(objectToPersist);
        MonetaryAdjustment objectResponse = monetaryAdjustmentRepository.save(objectToPersist);
        log.info("create - void: {}({}), {}({})", contract.getClass(), contract, objectResponse.getClass(), objectResponse);
        return objectResponse;
    }

    @Override
    public void applyContractAdjustment(MonetaryAdjustment monetaryAdjustment, BigDecimal medicalAuthorizationValue, MedicalAuthorizationItem medicalAuthorizationItem) {
        BigDecimal currentTransactionValue = medicalAuthorizationValue.add(medicalAuthorizationItem.getSubtotal());
        if (currentTransactionValue.compareTo(monetaryAdjustment.getThreshold()) > 0) {
            LocalDateTime from = DateUtils.resolvePeriodDateFrom(monetaryAdjustment.getPeriod());
            RestrictionMessage restrictionMessage = this.getRestrictionTypeValidator().buildRestrictionMessage(monetaryAdjustment.getClass().getSimpleName().toLowerCase(),
                    StringUtils.join("$", monetaryAdjustment.getThreshold().toString()),
                    StringUtils.join("$", currentTransactionValue.toString()),
                    this.buildRestrictionExtraMessage(monetaryAdjustment, from));
            medicalAuthorizationItem.setAuthorizationCondition(AuthorizationConditionReference.MONETARY_EXCEEDED.getInstance());
            this.applyRestriction(monetaryAdjustment, medicalAuthorizationItem, restrictionMessage);
        }
    }
}
