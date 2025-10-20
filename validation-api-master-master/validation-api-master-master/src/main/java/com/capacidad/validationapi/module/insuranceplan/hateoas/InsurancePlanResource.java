package com.capacidad.validationapi.module.insuranceplan.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.insuranceplan.controller.InsurancePlanController;
import com.capacidad.validationapi.module.insuranceplan.projection.InsurancePlanProjection;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_MEDICAL_COVERAGES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class InsurancePlanResource extends EntityModel<InsurancePlanProjection> {

    public InsurancePlanResource(InsurancePlanProjection insurancePlanProjection) throws ObjectNotFoundException, ObjectNotValidException {
        super(insurancePlanProjection);
        add(linkTo(methodOn(InsurancePlanController.class).getOne(insurancePlanProjection.getId())).withSelfRel());
        add(linkTo(methodOn(InsurancePlanController.class).getInsurancePlanMedicalCoverages(insurancePlanProjection.getId())).withRel(RESOURCE_MEDICAL_COVERAGES));
    }

}
