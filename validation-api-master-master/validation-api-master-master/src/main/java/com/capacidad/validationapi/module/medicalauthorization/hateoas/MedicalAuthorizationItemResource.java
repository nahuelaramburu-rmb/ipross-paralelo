package com.capacidad.validationapi.module.medicalauthorization.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.batch.controller.BatchItemController;
import com.capacidad.validationapi.module.batch.projection.BatchItemProjection;
import com.capacidad.validationapi.module.medicalauthorization.controller.MedicalAuthorizationItemController;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationItemProjection;
import com.capacidad.validationapi.module.medicalcoverage.controller.MedicalCoverageItemController;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageItemProjection;
import com.capacidad.validationapi.module.nomenclator.controller.NomenclatorController;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUDIT_LOGS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class MedicalAuthorizationItemResource extends EntityModel<MedicalAuthorizationItemProjection> {

    private final SelfModel<MedicalAuthorizationItemProjection.EmbeddedNomenclator, Long> nomenclator;
    private SelfModel<MedicalCoverageItemProjection.Parent, Long> medicalCoverageItem;
    private SelfModel<BatchItemProjection.BatchId, Long> batchItem;

    public MedicalAuthorizationItemResource(MedicalAuthorizationItemProjection medicalAuthorizationItemProjection) throws ObjectNotFoundException, ObjectNotValidException {
        super(medicalAuthorizationItemProjection);
        add(linkTo(methodOn(MedicalAuthorizationItemController.class).getOne(medicalAuthorizationItemProjection.getId())).withSelfRel());
        add(linkTo(methodOn(MedicalAuthorizationItemController.class).getAuditLogs(medicalAuthorizationItemProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        nomenclator = new SelfModel<>(medicalAuthorizationItemProjection.getNomenclator(), NomenclatorController.class);
        if (medicalAuthorizationItemProjection.getBatchItem() != null)
            batchItem = new SelfModel<>(medicalAuthorizationItemProjection.getBatchItem(), BatchItemController.class);
        if (medicalAuthorizationItemProjection.getMedicalCoverageItem() != null)
            medicalCoverageItem = new SelfModel<>(medicalAuthorizationItemProjection.getMedicalCoverageItem(), MedicalCoverageItemController.class);
    }

    public MedicalAuthorizationItemResource(MedicalAuthorizationItemProjection.WithParent medicalAuthorizationItemProjection) throws ObjectNotFoundException, ObjectNotValidException {
        super(medicalAuthorizationItemProjection);
        add(linkTo(methodOn(MedicalAuthorizationItemController.class).getOne(medicalAuthorizationItemProjection.getId())).withSelfRel());
        add(linkTo(methodOn(MedicalAuthorizationItemController.class).getAuditLogs(medicalAuthorizationItemProjection.getId())).withRel(RESOURCE_AUDIT_LOGS));
        nomenclator = new SelfModel<>(medicalAuthorizationItemProjection.getNomenclator(), NomenclatorController.class);
        if (medicalAuthorizationItemProjection.getBatchItem() != null)
            batchItem = new SelfModel<>(medicalAuthorizationItemProjection.getBatchItem(), BatchItemController.class);
        if (medicalAuthorizationItemProjection.getMedicalCoverageItem() != null)
            medicalCoverageItem = new SelfModel<>(medicalAuthorizationItemProjection.getMedicalCoverageItem(), MedicalCoverageItemController.class);
    }

    @JsonProperty("nomenclator")
    public SelfModel<MedicalAuthorizationItemProjection.EmbeddedNomenclator, Long> getNomenclator() {
        return nomenclator;
    }

    @JsonProperty("medicalCoverageItem")
    public SelfModel<MedicalCoverageItemProjection.Parent, Long> getMedicalCoverageItem() {
        return medicalCoverageItem;
    }

    @JsonProperty("batchItem")
    public SelfModel<BatchItemProjection.BatchId, Long> getBatchItem() {
        return batchItem;
    }

}
