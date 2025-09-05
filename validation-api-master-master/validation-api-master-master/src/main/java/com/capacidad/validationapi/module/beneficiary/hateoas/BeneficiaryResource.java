package com.capacidad.validationapi.module.beneficiary.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ResourceConstants;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.batch.controller.BatchController;
import com.capacidad.validationapi.module.beneficiary.controller.BeneficiaryController;
import com.capacidad.validationapi.module.beneficiary.controller.BeneficiaryInsurancePlanController;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryInsurancePlanProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.budget.controller.BeneficiaryBudgetController;
import com.capacidad.validationapi.module.disease.controller.ICD10DiseaseController;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationController;
import com.capacidad.validationapi.module.person.reference.RelationshipTypeReference;
import com.capacidad.validationapi.module.prescription.controller.PrescriptionController;
import com.capacidad.validationapi.module.tradeunion.controller.TradeUnionController;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingFunction.throwingFunction;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_NUMBER;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DEFAULT_PAGE_SIZE;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BeneficiaryResource extends EntityModel<BeneficiaryProjection> {

    private final Map<String, Object> embeddedResources = new HashMap<>();

    public BeneficiaryResource(BeneficiaryProjection beneficiaryProjection) throws ObjectNotFoundException, ObjectNotValidException {
        super(beneficiaryProjection);
        if (beneficiaryProjection.getRelatedBeneficiary() != null)
            add(linkTo(methodOn(BeneficiaryController.class).getOne(beneficiaryProjection.getRelatedBeneficiary().getId())).withRel(ResourceConstants.RESOURCE_RELATED_BENEFICIARY));
        if (beneficiaryProjection.getRelationshipType().getId().equals(RelationshipTypeReference.HOLDER.getId()))
            add(linkTo(methodOn(BeneficiaryBudgetController.class).getAllBeneficiaryBudgets(1, DEFAULT_PAGE_SIZE, beneficiaryProjection.getId(), "")).withRel(RESOURCE_BUDGETS).expand());
        List<SelfModel<BeneficiaryInsurancePlanProjection, Long>> embeddedInsurancePlans = new ArrayList<>();
        if (!beneficiaryProjection.getBeneficiaryInsurancePlans().isEmpty()) {
            embeddedInsurancePlans = beneficiaryProjection.getBeneficiaryInsurancePlans().stream()
                    .map(throwingFunction(i -> new SelfModel<>(i, BeneficiaryInsurancePlanController.class)))
                    .collect(Collectors.toUnmodifiableList());
        }
        List<SelfModel<ICD10DiseaseProjection, Long>> embeddedICD10Diseases = new ArrayList<>();
        if (!beneficiaryProjection.getDiseases().isEmpty()) {
            embeddedICD10Diseases = beneficiaryProjection.getDiseases().stream()
                    .map(throwingFunction(d -> new SelfModel<>(d, ICD10DiseaseController.class)))
                    .collect(Collectors.toUnmodifiableList());
        }
        List<SelfModel<IdAndNameOnlyProjection, Long>> embeddedTradeUnions = new ArrayList<>();
        if (!beneficiaryProjection.getTradeUnions().isEmpty()) {
            embeddedTradeUnions = beneficiaryProjection.getTradeUnions().stream()
                    .map(throwingFunction(t -> new SelfModel<>(t, TradeUnionController.class)))
                    .collect(Collectors.toUnmodifiableList());
        }
        String search = String.format("beneficiary:{id=%d}", beneficiaryProjection.getId());
        add(linkTo(methodOn(PrescriptionController.class).getAll(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, search)).withRel(RESOURCE_PRESCRIPTIONS));
        add(linkTo(methodOn(BeneficiaryController.class).getRelatives(beneficiaryProjection.getId())).withRel(ResourceConstants.RESOURCE_RELATIVES));
        add(linkTo(methodOn(BeneficiaryController.class).getOne(beneficiaryProjection.getId())).withSelfRel());
        add(linkTo(methodOn(BeneficiaryController.class).getExpirations(beneficiaryProjection.getId(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_EXPIRATIONS));
        add(linkTo(methodOn(BeneficiaryController.class).getContactInfo(beneficiaryProjection.getId())).withRel(ResourceConstants.RESOURCE_CONTACT_INFO));
        add(linkTo(methodOn(MedicalAuthorizationController.class).getBeneficiaryAuthorizations(beneficiaryProjection.getId(), 1, DEFAULT_PAGE_SIZE, null)).withRel(ResourceConstants.RESOURCE_AUTHORIZATIONS).expand());
        add(linkTo(methodOn(BeneficiaryController.class).getMonthlyGroupedCharges(beneficiaryProjection.getId(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_CHARGES));
        add(linkTo(methodOn(BeneficiaryController.class).getCompanyGroupedCharges(beneficiaryProjection.getId(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE)).withRel(RESOURCE_COMPANY_CHARGES));
        add(linkTo(methodOn(BatchController.class).getAll(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, search)).withRel(RESOURCE_BATCHES).expand());
        embeddedResources.put(RESOURCE_INSURANCE_PLANS, embeddedInsurancePlans);
        embeddedResources.put(RESOURCE_ICD10DISEASES, embeddedICD10Diseases);
        embeddedResources.put(RESOURCE_TRADE_UNIONS, embeddedTradeUnions);
    }

    @JsonUnwrapped
    @JsonProperty("_embedded")
    public Map<String, Object> getEmbeddedResources() {
        return this.embeddedResources;
    }

}
