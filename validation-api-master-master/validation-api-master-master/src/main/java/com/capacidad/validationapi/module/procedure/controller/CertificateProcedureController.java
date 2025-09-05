package com.capacidad.validationapi.module.procedure.controller;

import com.capacidad.validationapi.module.procedure.dto.CertificateProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.model.CertificateProcedure;
import com.capacidad.validationapi.module.procedure.model.CertificateType;
import com.capacidad.validationapi.module.procedure.service.CertificateProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_CERTIFICATE_PROCEDURES;

@RestController
@RequestMapping(value = ENDPOINT_CERTIFICATE_PROCEDURES)
public class CertificateProcedureController extends BaseProcedureControllerImpl<CertificateProcedureDTO, ProcedureResolutionDTO, CertificateProcedure> {

    private final CertificateProcedureService certificateProcedureService;

    @Autowired
    public CertificateProcedureController(CertificateProcedureService certificateProcedureService) {
        super(certificateProcedureService);
        this.certificateProcedureService = certificateProcedureService;
    }

    @GetMapping(value = "/certificate-types")
    public ResponseEntity<List<CertificateType>> getAllCertificateTypes() {
        return ResponseEntity.ok(certificateProcedureService.getAllCertificateTypes());
    }

}
