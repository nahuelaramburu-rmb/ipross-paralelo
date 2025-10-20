package com.capacidad.validationapi.module.batch.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public interface BatchProjection extends BaseProjection<Long> {

    interface Full extends BatchProjection {

        LocalDate getDateFrom();

        LocalDate getDateTo();

        IdAndNameOnlyProjection getStatus();

        @JsonIgnore
        Set<BatchItemProjection> getBatchItems();

        String getDescription();

        String getStatusUpdateDescription();

        @JsonIgnore
        BeneficiaryProjection.Minor getBeneficiary();

        Set<ICD10DiseaseProjection> getDiagnosis();

        LocalDateTime getCreatedAt();

    }

    interface Minor extends BatchProjection {

        LocalDate getDateFrom();

        LocalDate getDateTo();

        IdAndNameOnlyProjection getStatus();

        LocalDateTime getCreatedAt();

    }

    interface BeneficiaryId {

        BaseProjection<Long> getBeneficiary();

    }

}
