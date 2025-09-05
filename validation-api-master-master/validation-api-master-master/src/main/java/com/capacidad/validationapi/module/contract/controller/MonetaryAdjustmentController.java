package com.capacidad.validationapi.module.contract.controller;

import com.capacidad.validationapi.module.contract.dto.MonetaryAdjustmentDTO;
import com.capacidad.validationapi.module.contract.model.MonetaryAdjustment;
import com.capacidad.validationapi.module.contract.service.MonetaryAdjustmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_MONETARY_ADJUSTMENTS;

@RestController
@RequestMapping(value = ENDPOINT_MONETARY_ADJUSTMENTS)
public class MonetaryAdjustmentController extends BaseContractAdjustmentController<MonetaryAdjustment, MonetaryAdjustmentDTO> {

    @Autowired
    public MonetaryAdjustmentController(MonetaryAdjustmentService monetaryAdjustmentService) {
        super(monetaryAdjustmentService);
    }

}
