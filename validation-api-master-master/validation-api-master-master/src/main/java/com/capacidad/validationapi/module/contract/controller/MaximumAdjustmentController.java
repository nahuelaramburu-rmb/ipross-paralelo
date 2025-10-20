package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.validationapi.module.contract.dto.MaximumAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.MaximumAdjustment;
import com.capacidad.validationapi.module.contract.service.MaximumAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_MAXIMUM_ADJUSTMENTS;

@RestController
@RequestMapping(value = ENDPOINT_MAXIMUM_ADJUSTMENTS)
public class MaximumAdjustmentController extends BaseContractAdjustmentController<MaximumAdjustment, MaximumAdjustmentDTO> {

    @Autowired
    public MaximumAdjustmentController(MaximumAdjustmentService maximumAdjustmentService) {
        super(maximumAdjustmentService);
    }

}
