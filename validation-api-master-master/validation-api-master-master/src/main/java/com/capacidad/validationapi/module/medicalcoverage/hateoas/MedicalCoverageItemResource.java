package com.capacidad.validationapi.module.medicalcoverage.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalcoverage.controller.MedicalCoverageItemController;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageItemProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MedicalCoverageItemResource extends EntityModel<MedicalCoverageItemProjection> {
    public MedicalCoverageItemResource(MedicalCoverageItemProjection content) throws ObjectNotValidException, ObjectNotFoundException {
        super(content);
        add(linkTo(methodOn(MedicalCoverageItemController.class).getOne(content.getId())).withSelfRel());
        add(linkTo(methodOn(MedicalCoverageItemController.class).getAuditLogs(content.getId())).withRel(RESOURCE_AUDIT_LOGS));
    }
}
