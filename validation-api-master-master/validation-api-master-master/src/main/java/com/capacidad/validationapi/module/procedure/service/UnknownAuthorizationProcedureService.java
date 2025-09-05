package com.capacidad.validationapi.module.procedure.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.procedure.dto.ProcedureResolutionDTO;
import com.capacidad.validationapi.module.procedure.dto.UnknownAuthorizationProcedureDTO;
import com.capacidad.validationapi.module.procedure.model.UnknownAuthorizationProcedure;
import com.capacidad.validationapi.module.procedure.projection.UnknownAuthorizationProcedureProjection;

public interface UnknownAuthorizationProcedureService extends BaseProcedureService<UnknownAuthorizationProcedure, UnknownAuthorizationProcedureDTO, ProcedureResolutionDTO> {

    UnknownAuthorizationProcedureProjection findProcedureByAuthorizationId(long authorizationId) throws ObjectNotFoundException;

}
