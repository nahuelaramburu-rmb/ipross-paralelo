package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.validationapi.module.contract.dto.UsageRateAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.UsageRateAdjustment;
import com.capacidad.validationapi.module.contract.service.UsageRateAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_USAGE_RATE_ADJUSTMENTS;

@RestController
@RequestMapping(value = ENDPOINT_USAGE_RATE_ADJUSTMENTS)
public class UsageRateAdjustmentController extends BaseContractAdjustmentController<UsageRateAdjustment, UsageRateAdjustmentDTO> {

    @Autowired
    public UsageRateAdjustmentController(UsageRateAdjustmentService usageRateAdjustmentService) {
        super(usageRateAdjustmentService);
    }

}
