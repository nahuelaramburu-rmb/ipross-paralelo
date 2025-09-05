package com.capacidad.validationapi.module.contract.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.contract.controller.PractitionerContractController;
import com.capacidad.validationapi.module.contract.projection.PractitionerContractProjection;
import com.capacidad.validationapi.module.practitioner.controller.PractitionerController;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class PractitionerContractResource extends EntityModel<PractitionerContractProjection> {

    private final SelfModel<PractitionerProjection.Minor, Long> practitionerResource;

    public PractitionerContractResource(PractitionerContractProjection practitionerContractProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(practitionerContractProjection);
        add(linkTo(methodOn(PractitionerContractController.class).getOne(practitionerContractProjection.getId())).withSelfRel());
        add(linkTo(methodOn(ContractController.class).getAuditLogs(practitionerContractProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        add(linkTo(methodOn(ContractController.class).getAllFixedContractItems(practitionerContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_FIXED_CONTRACT_ITEMS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMaximumAdjustments(practitionerContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MAXIMUM_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllUsageRageAdjustment(practitionerContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_USAGE_RATE_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMonetaryAdjustments(practitionerContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MONETARY_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllAdjustments(practitionerContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_ADJUSTMENTS).expand());
        practitionerResource = new SelfModel<>(practitionerContractProjection.getPractitioner(), PractitionerController.class);
    }

    @JsonProperty("practitioner")
    public SelfModel<PractitionerProjection.Minor, Long> getPractitionerResource() {
        return this.practitionerResource;
    }

}
