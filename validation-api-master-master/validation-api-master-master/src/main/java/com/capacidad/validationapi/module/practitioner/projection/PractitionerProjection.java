package com.capacidad.validationapi.module.practitioner.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.projection.PersonProjection;
import com.capacidad.validationapi.module.rating.RatingProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface PractitionerProjection extends PersonProjection {

    String getPractitionerCode();

    IdAndNameOnlyProjection getPractitionerCategory();

    LocalDateTime getCreatedAt();

    @JsonIgnore
    Set<IdAndNameOnlyProjection> getMedicalSpecialties();

    @JsonIgnore
    Set<IdAndNameOnlyProjection> getContracts();

    UUID getResourceId();

    IdAndNameOnlyProjection getStatus();

    RatingProjection getRating();

    interface Minor extends PersonProjection {

        String getPractitionerCode();

        Set<IdAndNameOnlyProjection> getMedicalSpecialties();

    }

    interface Full extends PersonProjection {
        String getPractitionerCode();

        IdAndNameOnlyProjection getPractitionerCategory();

        LocalDateTime getCreatedAt();

        Set<IdAndNameOnlyProjection> getMedicalSpecialties();

        IdAndNameOnlyProjection getStatus();
    }
}
