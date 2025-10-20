package com.capacidad.validationapi.module.contract.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.contract.projection.ContractProjection;
import com.capacidad.validationapi.module.contract.projection.MedicalCenterContractProjection;
import com.capacidad.validationapi.module.contract.projection.OrganizationContractProjection;
import com.capacidad.validationapi.module.contract.projection.PractitionerContractProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ContractResource extends EntityModel<ContractProjection> {

    private SelfModel<IdAndNameOnlyProjection, Long> organizationResource;
    private SelfModel<IdAndNameOnlyProjection, Long> medicalCenterResource;
    private SelfModel<PractitionerProjection.Minor, Long> practitionerResource;

    public ContractResource(ContractProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(ContractController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(ContractController.class).getAuditLogs(projection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        add(linkTo(methodOn(ContractController.class).getAllFixedContractItems(projection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_FIXED_CONTRACT_ITEMS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMaximumAdjustments(projection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MAXIMUM_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllUsageRageAdjustment(projection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_USAGE_RATE_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMonetaryAdjustments(projection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MONETARY_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllAdjustments(projection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_ADJUSTMENTS).expand());
    }

    public ContractResource(OrganizationContractProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        OrganizationContractResource resource = new OrganizationContractResource(projection);
        add(resource.getLinks());
        organizationResource = resource.getOrganizationResource();
    }

    public ContractResource(MedicalCenterContractProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        MedicalCenterContractResource resource = new MedicalCenterContractResource(projection);
        add(resource.getLinks());
        medicalCenterResource = resource.getMedicalCenterResource();
    }

    public ContractResource(PractitionerContractProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        PractitionerContractResource resource = new PractitionerContractResource(projection);
        add(resource.getLinks());
        practitionerResource = resource.getPractitionerResource();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("organization")
    public SelfModel<IdAndNameOnlyProjection, Long> getOrganizationResource() {
        return this.organizationResource;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("medicalCenter")
    public SelfModel<IdAndNameOnlyProjection, Long> getMedicalCenterResource() {
        return this.medicalCenterResource;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("practitioner")
    public SelfModel<PractitionerProjection.Minor, Long> getPractitionerResource() {
        return this.practitionerResource;
    }

}
