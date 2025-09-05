package com.capacidad.validationapi.module.procedure.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.procedure.controller.CUDProcedureController;
import com.capacidad.validationapi.module.procedure.controller.ProcedureController;
import com.capacidad.validationapi.module.procedure.projection.CUDProcedureProjection;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_FILES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class CUDProcedureResource extends EntityModel<ProcedureProjection> {

    public CUDProcedureResource(CUDProcedureProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(CUDProcedureController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(CUDProcedureController.class).retrieveFileList(projection.getId())).withRel(RESOURCE_FILES));
        add(linkTo(methodOn(ProcedureController.class).getAuditLogs(projection.getId())).withRel(RESOURCE_AUDIT_LOGS));
    }

}
