package com.capacidad.validationapi.module.medicalcoverage.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalcoverage.controller.MedicalCoverageController;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_MEDICAL_COVERAGE_ITEMS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MedicalCoverageResource extends EntityModel<MedicalCoverageProjection> {

    public MedicalCoverageResource(MedicalCoverageProjection content) throws ObjectNotFoundException, ObjectNotValidException {
        super(content);
        add(linkTo(methodOn(MedicalCoverageController.class).getOne(content.getId())).withSelfRel());
        add(linkTo(methodOn(MedicalCoverageController.class).getMedicalCoverageItems(content.getId(), 1, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_MEDICAL_COVERAGE_ITEMS));
        add(linkTo(methodOn(MedicalCoverageController.class).getAuditLogs(content.getId())).withRel(RESOURCE_AUDIT_LOGS));
    }

}
