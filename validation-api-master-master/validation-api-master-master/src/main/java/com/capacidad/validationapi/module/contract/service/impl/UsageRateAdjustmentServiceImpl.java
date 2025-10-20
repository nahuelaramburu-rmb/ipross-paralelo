package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.contract.dto.UsageRateAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.UsageRateAdjustment;
import com.capacidad.validationapi.module.contract.repository.UsageRateAdjustmentRepository;
import com.capacidad.validationapi.module.contract.service.UsageRateAdjustmentService;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.model.RestrictionMessage;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationConditionReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Log4j2
@Service
public class UsageRateAdjustmentServiceImpl extends BaseContractAdjustmentServiceImpl<UsageRateAdjustment, UsageRateAdjustmentDTO> implements UsageRateAdjustmentService {

    private final UsageRateAdjustmentRepository usageRateAdjustmentRepository;

    @Autowired
    public UsageRateAdjustmentServiceImpl(UsageRateAdjustmentRepository repository) {
        super(repository);
        this.usageRateAdjustmentRepository = repository;
    }

    @Override
    public UsageRateAdjustment create(Contract contract, UsageRateAdjustmentDTO usageRateAdjustmentDTO) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({}), {}({})", contract.getClass(), contract, usageRateAdjustmentDTO.getClass(), usageRateAdjustmentDTO);
        UsageRateAdjustment objectToPersist = this.mapDtoToInput(usageRateAdjustmentDTO);
        objectToPersist.setContract(contract);
        this.validate(objectToPersist);
        UsageRateAdjustment objectResponse = usageRateAdjustmentRepository.save(objectToPersist);
        log.info("create - void: {}({}), {}({})", contract.getClass(), contract, objectResponse.getClass(), objectResponse);
        return objectResponse;
    }

    @Override
    public void applyContractAdjustment(UsageRateAdjustment usageRateAdjustment, BigDecimal medicalAuthorizationValue, MedicalAuthorizationItem medicalAuthorizationItem) {
        BigDecimal currentTransactionValue = medicalAuthorizationValue.add(new BigDecimal(medicalAuthorizationItem.getQuantity()));
        BigDecimal decimalCapitaAmount = new BigDecimal(usageRateAdjustment.getCapitaAmount());
        BigDecimal currentUsageRate = currentTransactionValue.divide(decimalCapitaAmount, 4, RoundingMode.HALF_UP);
        Period period = usageRateAdjustment.getPeriod();
        LocalDateTime from = DateUtils.resolvePeriodDateFrom(period);
        if (period == Period.MONTHLY)
            currentUsageRate = currentUsageRate.multiply(new BigDecimal(12));
        if ((period == Period.MONTHLY || period == Period.YEARLY) && currentUsageRate.compareTo(usageRateAdjustment.getThreshold()) > 0) {
            RestrictionMessage restrictionMessage = this.getRestrictionTypeValidator().buildRestrictionMessage(usageRateAdjustment.getClass().getSimpleName().toLowerCase(),
                    usageRateAdjustment.getThreshold().toString(),
                    String.valueOf(currentUsageRate),
                    this.buildRestrictionExtraMessage(usageRateAdjustment, from));
            medicalAuthorizationItem.setAuthorizationCondition(AuthorizationConditionReference.USAGE_RATE_EXCEEDED.getInstance());
            this.applyRestriction(usageRateAdjustment, medicalAuthorizationItem, restrictionMessage);
        }
    }

}
