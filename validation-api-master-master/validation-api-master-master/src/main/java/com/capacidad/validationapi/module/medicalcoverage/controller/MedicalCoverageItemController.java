package com.capacidad.validationapi.module.medicalcoverage.controller;

import com.capacidad.validationapi.module.base.controller.ReducedBaseControllerImpl;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageItemDTO;
import com.capacidad.validationapi.module.medicalcoverage.service.MedicalCoverageItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_MEDICAL_COVERAGE_ITEMS;

@RestController
@RequestMapping(value = ENDPOINT_MEDICAL_COVERAGE_ITEMS)
public class MedicalCoverageItemController extends ReducedBaseControllerImpl<MedicalCoverageItemDTO, Long> {

    @Autowired
    public MedicalCoverageItemController(MedicalCoverageItemService medicalCoverageItemService) {
        super(medicalCoverageItemService);
    }

}
