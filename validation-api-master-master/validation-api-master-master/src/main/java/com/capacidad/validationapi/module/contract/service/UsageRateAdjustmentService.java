package com.capacidad.validationapi.module.contract.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.contract.dto.UsageRateAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.UsageRateAdjustment;

public interface UsageRateAdjustmentService extends BaseContractAdjustmentService<UsageRateAdjustment, UsageRateAdjustmentDTO> {

    UsageRateAdjustment create(Contract contract, UsageRateAdjustmentDTO usageRateAdjustmentDTO) throws ObjectNotValidException, ObjectNotFoundException;

}
