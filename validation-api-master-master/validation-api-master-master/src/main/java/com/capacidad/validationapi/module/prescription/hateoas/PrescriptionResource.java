package com.capacidad.validationapi.module.prescription.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.beneficiary.controller.BeneficiaryController;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationController;
import com.capacidad.validationapi.module.medicalcenter.controller.MedicalCenterController;
import com.capacidad.validationapi.module.practitioner.controller.PractitionerController;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.prescription.controller.PrescriptionController;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionItemProjection;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class PrescriptionResource extends EntityModel<PrescriptionProjection> {

    private final SelfModel<BeneficiaryProjection.Minor, Long> beneficiary;
    private final SelfModel<PractitionerProjection.Minor, Long> practitioner;
    private final Set<PrescriptionItemProjection> embeddedPrescriptionItems;
    private SelfModel<IdAndNameOnlyProjection, Long> medicalCenter;
    private SelfModel<BaseProjection<Long>, Long> medicalAuthorization;

    public PrescriptionResource(PrescriptionProjection prescriptionProjection) throws ObjectNotFoundException, ObjectNotValidException {
        super(prescriptionProjection);
        add(linkTo(methodOn(PrescriptionController.class).getOne(prescriptionProjection.getId())).withSelfRel());
        add(linkTo(methodOn(PrescriptionController.class).getAuditLogs(prescriptionProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        add(linkTo(methodOn(PrescriptionController.class).getReceipt(prescriptionProjection.getId())).withRel(RESOURCE_RECEIPT));
        if (prescriptionProjection.getMedicalAuthorization() != null)
            medicalAuthorization = new SelfModel<>(prescriptionProjection.getMedicalAuthorization(), MedicalAuthorizationController.class);
        if (prescriptionProjection.getMedicalCenter() != null)
            medicalCenter = new SelfModel<>(prescriptionProjection.getMedicalCenter(), MedicalCenterController.class);
        beneficiary = new SelfModel<>(prescriptionProjection.getBeneficiary(), BeneficiaryController.class);
        practitioner = new SelfModel<>(prescriptionProjection.getPractitioner(), PractitionerController.class);
        embeddedPrescriptionItems = prescriptionProjection.getPrescriptionItems();
    }

    @JsonProperty("medicalAuthorization")
    public SelfModel<BaseProjection<Long>, Long> getMedicalAuthorization() {
        return medicalAuthorization;
    }

    @JsonProperty("medicalCenter")
    public SelfModel<IdAndNameOnlyProjection, Long> getMedicalCenter() {
        return medicalCenter;
    }

    @JsonProperty("beneficiary")
    public SelfModel<BeneficiaryProjection.Minor, Long> getBeneficiary() {
        return beneficiary;
    }

    @JsonProperty("practitioner")
    public SelfModel<PractitionerProjection.Minor, Long> getPractitioner() {
        return practitioner;
    }

    @JsonUnwrapped
    public CollectionModelWrapper<PrescriptionItemProjection> getEmbeddedAuthorizationItems() {
        return new CollectionModelWrapper<>(RESOURCE_PRESCRIPTION_ITEMS, embeddedPrescriptionItems);
    }

}
