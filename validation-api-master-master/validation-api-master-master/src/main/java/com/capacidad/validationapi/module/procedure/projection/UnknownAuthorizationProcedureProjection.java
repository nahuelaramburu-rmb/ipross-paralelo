package com.capacidad.validationapi.module.procedure.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

public interface UnknownAuthorizationProcedureProjection extends ProcedureProjection {

    BaseProjection<Long> getMedicalAuthorization();

}
