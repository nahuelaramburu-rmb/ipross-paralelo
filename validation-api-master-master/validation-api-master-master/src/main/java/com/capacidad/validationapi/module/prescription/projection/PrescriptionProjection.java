package com.capacidad.validationapi.module.prescription.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public interface PrescriptionProjection extends BaseProjection<Long> {

    @JsonIgnore
    BeneficiaryProjection.Minor getBeneficiary();

    @JsonIgnore
    PractitionerProjection.Minor getPractitioner();

    @JsonIgnore
    IdAndNameOnlyProjection getMedicalCenter();

    IdAndNameOnlyProjection getStatus();

    String getObservations();

    Period getExpirationPeriod();

    @JsonIgnore
    Set<PrescriptionItemProjection> getPrescriptionItems();

    Set<Long> getExchangeId();

    @JsonIgnore
    BaseProjection<Long> getMedicalAuthorization();

    LocalDateTime getCreatedAt();

    String getCancellationReason();

    LocalDate getExpirationDate();

    Boolean getPreAuthorized();

    interface Minor extends BaseProjection<Long> {

        Set<Long> getExchangeId();

        IdAndNameOnlyProjection getStatus();

        String getObservations();

        Period getExpirationPeriod();

        Set<PrescriptionItemProjection> getPrescriptionItems();

        LocalDate getExpirationDate();

        Boolean getPreAuthorized();
    }

    interface Integration {

        Set<Long> getExchangeId();

        Set<PrescriptionItemProjection.Integration> getPrescriptionItems();

        LocalDate getExpirationDate();

    }

}
