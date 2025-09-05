package com.capacidad.validationapi.module.procedure.controller;

import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.model.DisabilityProcedure;
import com.capacidad.validationapi.module.procedure.service.DisabilityProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_DISABILITY_PROCEDURES;

@RestController
@RequestMapping(value = ENDPOINT_DISABILITY_PROCEDURES)
public class DisabilityProcedureController extends BaseProcedureControllerImpl<ProcedureDTO, ProcedureResolutionDTO, DisabilityProcedure> {
    @Autowired
    public DisabilityProcedureController(DisabilityProcedureService disabilityProcedureService) {
        super(disabilityProcedureService);
    }
}
