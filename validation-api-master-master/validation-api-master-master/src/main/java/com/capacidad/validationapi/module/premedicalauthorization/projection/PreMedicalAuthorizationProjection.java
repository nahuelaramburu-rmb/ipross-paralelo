package com.capacidad.validationapi.module.premedicalauthorization.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.projection.PersonProjection;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Set;

public interface PreMedicalAuthorizationProjection extends BaseProjection<Long> {
    String getCode();

    BeneficiaryProjection getBeneficiary();

    PersonProjection getPetitioner();

    @JsonProperty("medicalAuthorizationItems")
    Set<PreMedicalAuthorizationItemProjection> getPreMedicalAuthorizationItems();

    IdAndNameOnlyProjection getStatus();

    LocalDate getExpirationDate();

    interface Minor extends BaseProjection<Long> {
        String getCode();
    }

}
