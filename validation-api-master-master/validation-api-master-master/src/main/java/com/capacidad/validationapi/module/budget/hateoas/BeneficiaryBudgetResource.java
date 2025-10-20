package com.capacidad.validationapi.module.budget.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.beneficiary.controller.BeneficiaryController;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.budget.controller.BeneficiaryBudgetController;
import com.capacidad.validationapi.module.budget.projection.BeneficiaryBudgetProjection;
import com.capacidad.validationapi.module.company.controller.CompanyController;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_BUDGET_ITEMS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_RECEIPT;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BeneficiaryBudgetResource extends EntityModel<BeneficiaryBudgetProjection> {

    private final SelfModel<IdAndNameOnlyProjection, Long> company;
    private final SelfModel<BeneficiaryProjection.Minor, Long> beneficiary;

    public BeneficiaryBudgetResource(BeneficiaryBudgetProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        add(linkTo(methodOn(BeneficiaryBudgetController.class).getReceipt(projection.getId())).withRel(RESOURCE_RECEIPT));
        add(linkTo(methodOn(BeneficiaryBudgetController.class).getOne(projection.getId())).withSelfRel());
        add(linkTo(methodOn(BeneficiaryBudgetController.class).getBudgetItems(1, DEFAULT_PAGE_SIZE, projection.getId(), "")).withRel(RESOURCE_BUDGET_ITEMS).expand());
        company = new SelfModel<>(projection.getCompany(), CompanyController.class);
        beneficiary = new SelfModel<>(projection.getBeneficiary(), BeneficiaryController.class);
    }

    @JsonProperty("company")
    public SelfModel<IdAndNameOnlyProjection, Long> getCompany() {
        return company;
    }

    @JsonProperty("beneficiary")
    public SelfModel<BeneficiaryProjection.Minor, Long> getBeneficiary() {
        return beneficiary;
    }

}
