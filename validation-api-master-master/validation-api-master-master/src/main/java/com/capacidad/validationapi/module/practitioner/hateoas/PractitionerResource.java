package com.capacidad.validationapi.module.practitioner.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ResourceConstants;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.budget.controller.PractitionerBudgetController;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationController;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationItemController;
import com.capacidad.validationapi.module.practitioner.controller.PractitionerController;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.prescription.controller.PrescriptionController;
import com.capacidad.validationapi.module.settlement.controller.SettlementController;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class PractitionerResource extends EntityModel<PractitionerProjection> {

    private final Map<String, Object> embeddedResources = new HashMap<>();

    public PractitionerResource(PractitionerProjection practitionerProjection) throws ObjectNotFoundException, ObjectNotValidException {
        super(practitionerProjection);
        add(linkTo(methodOn(PractitionerController.class).getOne(practitionerProjection.getId())).withSelfRel());
        add(linkTo(methodOn(PractitionerController.class).getContactInfo(practitionerProjection.getId())).withRel(ResourceConstants.RESOURCE_CONTACT_INFO));
        add(linkTo(methodOn(MedicalAuthorizationController.class).getPractitionerAuthorizations(practitionerProjection.getId(), 1, DEFAULT_PAGE_SIZE, null)).withRel(ResourceConstants.RESOURCE_AUTHORIZATIONS).expand());
        add(linkTo(methodOn(PractitionerBudgetController.class).getAllPractitionerBudgets(1, DEFAULT_PAGE_SIZE, practitionerProjection.getId(), "")).withRel(RESOURCE_BUDGETS).expand());
        add(linkTo(methodOn(PractitionerController.class).getPractitionerMedicalCenters(practitionerProjection.getId())).withRel(ResourceConstants.RESOURCE_MEDICAL_CENTERS));
        add(linkTo(methodOn(PractitionerController.class).getMedicalRegistrations(practitionerProjection.getId())).withRel(ResourceConstants.RESOURCE_MEDICAL_REGISTRATIONS));
        String settlementSearch = String.format("practitioner:{id=%d}", practitionerProjection.getId());
        String medAuthItemsSearch = String.format("medicalAuthorization.practitioner:{id=%d}", practitionerProjection.getId());
        add(linkTo(methodOn(MedicalAuthorizationItemController.class).getAll(1, DEFAULT_PAGE_SIZE, medAuthItemsSearch)).withRel(ResourceConstants.RESOURCE_AUTHORIZATION_ITEMS));
        add(linkTo(methodOn(SettlementController.class).getAll(1, DEFAULT_PAGE_SIZE, settlementSearch)).withRel(RESOURCE_SETTLEMENTS));
        String search = String.format("practitioner:{id=%d}", practitionerProjection.getId());
        add(linkTo(methodOn(PrescriptionController.class).getAll(1, DEFAULT_PAGE_SIZE, search)).withRel(RESOURCE_PRESCRIPTIONS));
        List<EntityModel<IdAndNameOnlyProjection>> embeddedMedicalSpecialties = practitionerProjection.getMedicalSpecialties().stream()
                .map(EntityModel<IdAndNameOnlyProjection>::new)
                .collect(Collectors.toUnmodifiableList());
        List<SelfModel<IdAndNameOnlyProjection, Long>> embeddedContracts = new ArrayList<>();
        for (IdAndNameOnlyProjection c : practitionerProjection.getContracts())
            embeddedContracts.add(new SelfModel<>(c, ContractController.class));
        embeddedResources.put(RESOURCE_MEDICAL_SPECIALTIES, embeddedMedicalSpecialties);
        embeddedResources.put(RESOURCE_CONTRACTS, embeddedContracts);
    }

    @JsonUnwrapped
    @JsonProperty("_embedded")
    public Map<String, Object> getEmbeddedResources() {
        return embeddedResources;
    }

}
