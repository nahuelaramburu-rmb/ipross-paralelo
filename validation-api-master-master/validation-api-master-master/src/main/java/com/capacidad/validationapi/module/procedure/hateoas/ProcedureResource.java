package com.capacidad.validationapi.module.procedure.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.projection.*;
import org.springframework.hateoas.EntityModel;

public class ProcedureResource extends EntityModel<ProcedureProjection> {

    public ProcedureResource(CertificateProcedureProjection procedureProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(procedureProjection);
        CertificateProcedureResource resource = new CertificateProcedureResource(procedureProjection);
        add(resource.getLinks());
    }

    public ProcedureResource(CUDProcedureProjection procedureProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(procedureProjection);
        CUDProcedureResource resource = new CUDProcedureResource(procedureProjection);
        add(resource.getLinks());
    }

    public ProcedureResource(DisabilityProcedureProjection procedureProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(procedureProjection);
        DisabilityProcedureResource resource = new DisabilityProcedureResource(procedureProjection);
        add(resource.getLinks());
    }

    public ProcedureResource(UnknownAuthorizationProcedureProjection procedureProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(procedureProjection);
        UnknownAuthorizationProcedureResource resource = new UnknownAuthorizationProcedureResource(procedureProjection);
        add(resource.getLinks());
    }

}
