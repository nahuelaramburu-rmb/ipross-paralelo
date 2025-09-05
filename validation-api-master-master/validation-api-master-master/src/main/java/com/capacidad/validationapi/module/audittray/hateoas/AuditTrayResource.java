package com.capacidad.validationapi.module.audittray.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.controller.AuditTrayController;
import com.capacidad.validationapi.module.audittray.projection.AuditTrayProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDITORS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_NOMENCLATORS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AuditTrayResource extends EntityModel<AuditTrayProjection> {

    public AuditTrayResource(AuditTrayProjection auditTrayProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(auditTrayProjection);
        add(linkTo(methodOn(AuditTrayController.class).getOne(auditTrayProjection.getId())).withSelfRel());
        add(linkTo(methodOn(AuditTrayController.class).getAuditTrayNomenclators(auditTrayProjection.getId(), 1, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_NOMENCLATORS));
        add(linkTo(methodOn(AuditTrayController.class).getAuditTrayAuditors(auditTrayProjection.getId(), 1, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_AUDITORS));
    }

    public AuditTrayResource(AuditTrayProjection.Extended auditTrayProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(auditTrayProjection);
        add(linkTo(methodOn(AuditTrayController.class).getOne(auditTrayProjection.getId())).withSelfRel());
        add(linkTo(methodOn(AuditTrayController.class).getAuditTrayNomenclators(auditTrayProjection.getId(), 1, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_NOMENCLATORS));
        add(linkTo(methodOn(AuditTrayController.class).getAuditTrayAuditors(auditTrayProjection.getId(), 1, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_AUDITORS));
    }

}
