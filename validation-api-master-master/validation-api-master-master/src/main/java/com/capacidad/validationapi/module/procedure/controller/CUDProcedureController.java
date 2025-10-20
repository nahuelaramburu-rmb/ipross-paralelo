package com.capacidad.validationapi.module.procedure.controller;

import com.capacidad.validationapi.module.procedure.dto.CUDProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.capacidad.validationapi.module.procedure.model.CUDProcedure;
import com.capacidad.validationapi.module.procedure.service.CUDProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_CUD_PROCEDURES;

@RestController
@RequestMapping(value = ENDPOINT_CUD_PROCEDURES)
public class CUDProcedureController extends BaseProcedureControllerImpl<ProcedureDTO, CUDProcedureResolutionDTO, CUDProcedure> {
    @Autowired
    public CUDProcedureController(CUDProcedureService cudProcedureService) {
        super(cudProcedureService);
    }
}
