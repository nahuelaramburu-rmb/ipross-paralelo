package com.capacidad.validationapi.module.procedure.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.controller.DisabilityProcedureController;
import com.capacidad.validationapi.module.procedure.controller.ProcedureController;
import com.capacidad.validationapi.module.procedure.projection.DisabilityProcedureProjection;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_FILES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class DisabilityProcedureResource extends EntityModel<ProcedureProjection> {

    public DisabilityProcedureResource(DisabilityProcedureProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(DisabilityProcedureController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(DisabilityProcedureController.class).retrieveFileList(projection.getId())).withRel(RESOURCE_FILES));
        add(linkTo(methodOn(ProcedureController.class).getAuditLogs(projection.getId())).withRel(RESOURCE_AUDIT_LOGS));
    }

}
