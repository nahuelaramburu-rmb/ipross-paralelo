package com.capacidad.validationapi.module.contract.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.contract.controller.MedicalCenterContractController;
import com.capacidad.validationapi.module.contract.projection.MedicalCenterContractProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalcenter.controller.MedicalCenterController;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MedicalCenterContractResource extends EntityModel<MedicalCenterContractProjection> {

    private final SelfModel<IdAndNameOnlyProjection, Long> medicalCenterResource;

    public MedicalCenterContractResource(MedicalCenterContractProjection medicalCenterContractProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(medicalCenterContractProjection);
        add(linkTo(methodOn(MedicalCenterContractController.class).getOne(medicalCenterContractProjection.getId())).withSelfRel());
        add(linkTo(methodOn(ContractController.class).getAuditLogs(medicalCenterContractProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        add(linkTo(methodOn(ContractController.class).getAllFixedContractItems(medicalCenterContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_FIXED_CONTRACT_ITEMS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMaximumAdjustments(medicalCenterContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MAXIMUM_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllUsageRageAdjustment(medicalCenterContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_USAGE_RATE_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllMonetaryAdjustments(medicalCenterContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_MONETARY_ADJUSTMENTS).expand());
        add(linkTo(methodOn(ContractController.class).getAllAdjustments(medicalCenterContractProjection.getId(), 1, DEFAULT_PAGE_SIZE, "")).withRel(RESOURCE_ADJUSTMENTS).expand());
        medicalCenterResource = new SelfModel<>(medicalCenterContractProjection.getMedicalCenter(), MedicalCenterController.class);
    }

    @JsonProperty("medicalCenter")
    public SelfModel<IdAndNameOnlyProjection, Long> getMedicalCenterResource() {
        return this.medicalCenterResource;
    }
}
