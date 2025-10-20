package com.capacidad.validationapi.module.procedure.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface CertificateProcedureProjection extends ProcedureProjection {

    IdAndNameOnlyProjection getCertificateType();

}
