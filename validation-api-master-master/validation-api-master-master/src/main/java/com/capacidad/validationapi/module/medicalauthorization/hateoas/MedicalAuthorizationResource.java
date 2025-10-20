package com.capacidad.validationapi.module.medicalauthorization.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.batch.controller.BatchController;
import com.capacidad.validationapi.module.beneficiary.controller.BeneficiaryController;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.company.controller.CompanyController;
import com.capacidad.validationapi.module.contract.controller.ContractController;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationController;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalcenter.controller.MedicalCenterController;
import com.capacidad.validationapi.module.practitioner.controller.PractitionerController;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.prescription.controller.PrescriptionController;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingFunction.throwingFunction;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MedicalAuthorizationResource extends EntityModel<MedicalAuthorizationProjection> {

    private final Map<String, Object> embeddedResources = new HashMap<>();
    private final SelfModel<IdAndNameOnlyProjection, Long> medicalCenter;
    private final SelfModel<IdAndNameOnlyProjection, Long> contract;
    private final SelfModel<BeneficiaryProjection.Minor, Long> beneficiary;
    private final SelfModel<PractitionerProjection.Minor, Long> practitioner;
    private SelfModel<IdAndNameOnlyProjection, Long> company;
    private SelfModel<PractitionerProjection.Minor, Long> petitioner;
    private SelfModel<BaseProjection<Long>, Long> batch;
    private List<SelfModel<PrescriptionProjection.Minor, Long>> embeddedPrescriptions;

    public MedicalAuthorizationResource(MedicalAuthorizationProjection medicalAuthorizationProjection) throws ObjectNotFoundException, ObjectNotValidException {
        super(medicalAuthorizationProjection);
        add(linkTo(methodOn(MedicalAuthorizationController.class).getOne(medicalAuthorizationProjection.getId())).withSelfRel());
        add(linkTo(methodOn(MedicalAuthorizationController.class).getAuditLogs(medicalAuthorizationProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        add(linkTo(methodOn(MedicalAuthorizationController.class).getReceipt(medicalAuthorizationProjection.getId())).withRel(RESOURCE_RECEIPT));
        add(linkTo(methodOn(MedicalAuthorizationController.class).getAllMessages(medicalAuthorizationProjection.getId())).withRel(RESOURCE_MESSAGES));
        add(linkTo(methodOn(MedicalAuthorizationController.class).getDiagnosis(medicalAuthorizationProjection.getId())).withRel(RESOURCE_DIAGNOSIS));
        medicalCenter = new SelfModel<>(medicalAuthorizationProjection.getMedicalCenter(), MedicalCenterController.class);
        if (medicalAuthorizationProjection.getCompany() != null)
            company = new SelfModel<>(medicalAuthorizationProjection.getCompany(), CompanyController.class);
        if (medicalAuthorizationProjection.getBatch() != null)
            batch = new SelfModel<>(medicalAuthorizationProjection.getBatch(), BatchController.class);
        if (!medicalAuthorizationProjection.getPrescriptions().isEmpty())
            embeddedPrescriptions = medicalAuthorizationProjection.getPrescriptions().stream()
                    .map(throwingFunction(p -> new SelfModel<>(p, PrescriptionController.class)))
                    .collect(Collectors.toUnmodifiableList());
        contract = new SelfModel<>(medicalAuthorizationProjection.getContract(), ContractController.class);
        beneficiary = new SelfModel<>(medicalAuthorizationProjection.getBeneficiary(), BeneficiaryController.class);
        practitioner = new SelfModel<>(medicalAuthorizationProjection.getPractitioner(), PractitionerController.class);
        if (medicalAuthorizationProjection.getPetitioner() != null)
            petitioner = new SelfModel<>(medicalAuthorizationProjection.getPetitioner(), PractitionerController.class);
        List<MedicalAuthorizationItemResource> embeddedAuthorizationItems = medicalAuthorizationProjection.getMedicalAuthorizationItems().stream()
                .map(throwingFunction(MedicalAuthorizationItemResource::new))
                .collect(Collectors.toUnmodifiableList());
        embeddedResources.put(RESOURCE_AUTHORIZATION_ITEMS, embeddedAuthorizationItems);
        embeddedResources.put(RESOURCE_PRESCRIPTIONS, embeddedPrescriptions);
    }

    @JsonProperty("medicalCenter")
    public SelfModel<IdAndNameOnlyProjection, Long> getMedicalCenter() {
        return medicalCenter;
    }

    @JsonProperty("company")
    public SelfModel<IdAndNameOnlyProjection, Long> getCompany() {
        return company;
    }

    @JsonProperty("batch")
    public SelfModel<BaseProjection<Long>, Long> getBatch() {
        return batch;
    }

    @JsonProperty("contract")
    public SelfModel<IdAndNameOnlyProjection, Long> getContract() {
        return contract;
    }

    @JsonProperty("beneficiary")
    public SelfModel<BeneficiaryProjection.Minor, Long> getBeneficiary() {
        return beneficiary;
    }

    @JsonProperty("practitioner")
    public SelfModel<PractitionerProjection.Minor, Long> getPractitioner() {
        return practitioner;
    }

    @JsonProperty("petitioner")
    public SelfModel<PractitionerProjection.Minor, Long> getPetitioner() {
        return petitioner;
    }

    @JsonUnwrapped
    @JsonProperty("_embedded")
    public Map<String, Object> getEmbeddedResources() {
        return embeddedResources;
    }

}
