package com.capacidad.validationapi.module.batch.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.SelfModel;
import com.capacidad.validationapi.module.batch.controller.BatchItemController;
import com.capacidad.validationapi.module.batch.projection.BatchItemProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalcenter.controller.MedicalCenterController;
import com.capacidad.validationapi.module.nomenclator.controller.NomenclatorController;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.capacidad.validationapi.module.practitioner.controller.PractitionerController;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_MEDICAL_CENTERS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_PRACTITIONERS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BatchItemResource extends EntityModel<BatchItemProjection> {

    private final SelfModel<NomenclatorProjection.Minor, Long> nomenclator;
    private final Map<String, Object> embeddedResources = new HashMap<>();

    public BatchItemResource(BatchItemProjection content) throws ObjectNotValidException, ObjectNotFoundException {
        super(content);
        add(linkTo(methodOn(BatchItemController.class).getOne(content.getId())).withSelfRel());
        nomenclator = new SelfModel<>(content.getNomenclator(), NomenclatorController.class);
        List<SelfModel<PractitionerProjection.Minor, Long>> embeddedPractitioners = new ArrayList<>();
        for (PractitionerProjection.Minor p : content.getPractitioners())
            embeddedPractitioners.add(new SelfModel<>(p, PractitionerController.class));
        List<SelfModel<IdAndNameOnlyProjection, Long>> embeddedMedicalCenters = new ArrayList<>();
        for (IdAndNameOnlyProjection m : content.getMedicalCenters())
            embeddedMedicalCenters.add(new SelfModel<>(m, MedicalCenterController.class));
        embeddedResources.put(RESOURCE_PRACTITIONERS, embeddedPractitioners);
        embeddedResources.put(RESOURCE_MEDICAL_CENTERS, embeddedMedicalCenters);
    }

    @JsonProperty("nomenclator")
    public SelfModel<NomenclatorProjection.Minor, Long> getNomenclator() {
        return nomenclator;
    }

    @JsonUnwrapped
    @JsonProperty("_embedded")
    public Map<String, Object> getEmbeddedResources() {
        return embeddedResources;
    }

}
