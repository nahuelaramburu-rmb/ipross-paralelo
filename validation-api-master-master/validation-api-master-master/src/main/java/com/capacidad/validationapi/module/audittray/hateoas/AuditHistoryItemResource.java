package com.capacidad.validationapi.module.audittray.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.projection.AuditHistoryItemProjection;
import com.capacidad.validationapi.module.medicalauthorization.hateoas.MedicalAuthorizationItemResource;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

public class AuditHistoryItemResource extends EntityModel<AuditHistoryItemProjection> {

    private final MedicalAuthorizationItemResource authorizationItemResource;

    public AuditHistoryItemResource(AuditHistoryItemProjection projection) throws ObjectNotValidException, ObjectNotFoundException {
        super(projection);
        authorizationItemResource = new MedicalAuthorizationItemResource(projection.getMedicalAuthorizationItem());
    }

    @JsonUnwrapped
    public MedicalAuthorizationItemResource getAuthorizationItemResource() {
        return authorizationItemResource;
    }


}
