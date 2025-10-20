package com.capacidad.validationapi.module.procedure.service;

import com.capacidad.validationapi.module.procedure.dto.CertificateProcedureDTO;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.model.CertificateProcedure;
import com.capacidad.validationapi.module.procedure.model.CertificateType;

import java.util.List;

public interface CertificateProcedureService extends BaseProcedureService<CertificateProcedure, CertificateProcedureDTO, ProcedureResolutionDTO> {

    List<CertificateType> getAllCertificateTypes();

}
