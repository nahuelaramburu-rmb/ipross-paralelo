package com.capacidad.validationapi.module.budget.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.budget.controller.PractitionerBudgetController;
import com.capacidad.validationapi.module.budget.projection.PractitionerBudgetProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalcenter.controller.MedicalCenterController;
import com.capacidad.validationapi.module.practitioner.controller.PractitionerController;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_BUDGET_ITEMS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_RECEIPT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class PractitionerBudgetResource extends EntityModel<PractitionerBudgetProjection> {

    private final SelfModel<IdAndNameOnlyProjection, Long> medicalCenter;
    private final SelfModel<PractitionerProjection.Minor, Long> practitioner;

    public PractitionerBudgetResource(PractitionerBudgetProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(PractitionerBudgetController.class).getReceipt(projection.getId())).withRel(RESOURCE_RECEIPT));
        add(linkTo(methodOn(PractitionerBudgetController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(PractitionerBudgetController.class).getBudgetItems(1, DEFAULT_PAGE_SIZE, projection.getId(), "")).withRel(RESOURCE_BUDGET_ITEMS).expand());
        medicalCenter = new SelfModel<>(projection.getMedicalCenter(), MedicalCenterController.class);
        practitioner = new SelfModel<>(projection.getPractitioner(), PractitionerController.class);
    }

    @JsonProperty("medicalCenter")
    public SelfModel<IdAndNameOnlyProjection, Long> getMedicalCenter() {
        return medicalCenter;
    }

    @JsonProperty("practitioner")
    public SelfModel<PractitionerProjection.Minor, Long> getPractitioner() {
        return practitioner;
    }

}
