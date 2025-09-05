package com.capacidad.validationapi.module.prescription.controller;

import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionRestrictionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_PRESCRIPTION_RESTRICTIONS;

@RestController
@RequestMapping(value = ENDPOINT_PRESCRIPTION_RESTRICTIONS)
public class PrescriptionRestrictionController extends BaseControllerImpl<PrescriptionRestrictionDTO, Long> {

    @Autowired
    public PrescriptionRestrictionController(BaseService<? extends BaseEntity<Long>, PrescriptionRestrictionDTO, Long> abstractService) {
        super(abstractService);
    }
}
