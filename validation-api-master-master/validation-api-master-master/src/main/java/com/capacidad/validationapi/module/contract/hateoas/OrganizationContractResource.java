package com.capacidad.validationapi.module.contract.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.contract.controller.OrganizationContractController;
import com.capacidad.validationapi.module.contract.projection.OrganizationContractProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.organization.controller.OrganizationController;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class OrganizationContractResource extends EntityModel<OrganizationContractProjection> {

    private final SelfModel<IdAndNameOnlyProjection, Long> organizationResource;

    public OrganizationContractResource(OrganizationContractProjection organizationContractProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(organizationContractProjection);
        add(linkTo(methodOn(OrganizationContractController.class).getOne(organizationContractProjection.getId())).withSelfRel());
        add(linkTo(methodOn(ContractController.class).getAuditLogs(organizationContractProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        add(linkTo(methodOn(ContractController.class).getAllFixedContractItems(organizationContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_FIXED_CONTRACT_ITEMS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMaximumAdjustments(organizationContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MAXIMUM_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllUsageRageAdjustment(organizationContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_USAGE_RATE_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMonetaryAdjustments(organizationContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MONETARY_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllAdjustments(organizationContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_ADJUSTMENTS).expand());
        organizationResource = new SelfModel<>(organizationContractProjection.getOrganization(), OrganizationController.class);
    }

    @JsonProperty("organization")
    public SelfModel<IdAndNameOnlyProjection, Long> getOrganizationResource() {
        return this.organizationResource;
    }

}
