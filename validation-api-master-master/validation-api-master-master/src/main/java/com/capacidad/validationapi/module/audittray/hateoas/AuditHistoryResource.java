package com.capacidad.validationapi.module.audittray.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.controller.AuditHistoryController;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryProjection;
import com.capacidad.validationapi.module.medicalauthorization.hateoas.MedicalAuthorizationResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.EntityModel;

import java.util.List;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingFunction.throwingFunction;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_AUTHORIZATION_ITEMS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AuditHistoryResource extends EntityModel<AuditHistoryProjection> {

    private final MedicalAuthorizationResource authorizationResource;

    public AuditHistoryResource(AuditHistoryProjection auditHistoryProjection) throws ObjectNotValidException, ObjectNotFoundException {
        super(auditHistoryProjection);
        add(linkTo(methodOn(AuditHistoryController.class).getAuditHistory(auditHistoryProjection.getId())).withSelfRel());
        authorizationResource = new MedicalAuthorizationResource(auditHistoryProjection.getMedicalAuthorization());
        List<AuditHistoryItemResource> embeddedAuthorizationItems = auditHistoryProjection.getHistoryItems().stream()
                .map(throwingFunction(AuditHistoryItemResource::new))
                .collect(Collectors.toUnmodifiableList());
        authorizationResource.getEmbeddedResources().put(RESOURCE_AUTHORIZATION_ITEMS, embeddedAuthorizationItems);
    }

    @JsonProperty("authorization")
    public MedicalAuthorizationResource getAuthorizationResource() {
        return authorizationResource;
    }

}
