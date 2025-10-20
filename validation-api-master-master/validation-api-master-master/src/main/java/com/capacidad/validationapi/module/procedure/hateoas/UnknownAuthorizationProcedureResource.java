package com.capacidad.validationapi.module.procedure.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationController;
import com.capacidad.validationapi.module.procedure.controller.ProcedureController;
import com.capacidad.validationapi.module.procedure.controller.UnknownAuthorizationProcedureController;
import com.capacidad.validationapi.module.procedure.projection.ProcedureProjection;
import com.capacidad.validationapi.module.procedure.projection.UnknownAuthorizationProcedureProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class UnknownAuthorizationProcedureResource extends EntityModel<ProcedureProjection> {

    public UnknownAuthorizationProcedureResource(UnknownAuthorizationProcedureProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(UnknownAuthorizationProcedureController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(UnknownAuthorizationProcedureController.class).retrieveFileList(projection.getId())).withRel(RESOURCE_FILES));
        add(linkTo(methodOn(ProcedureController.class).getAuditLogs(projection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        add(linkTo(methodOn(MedicalAuthorizationController.class).getOne(projection.getMedicalAuthorization().getId())).withRel(RESOURCE_AUTHORIZATION));
    }

}
