package com.capacidad.validationapi.module.medicalauthorization.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalauthorization.config.MedicalAuthorizationProjectionQRSerializer;
import com.capacidad.validationapi.module.person.projection.PersonProjection;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.premedicalauthorization.projection.PreMedicalAuthorizationProjection;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionProjection;
import com.capacidad.validationapi.module.rating.RatingProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public interface MedicalAuthorizationProjection extends BaseProjection<Long> {

    LocalDateTime getCreatedAt();

    @JsonIgnore
    IdAndNameOnlyProjection getContract();

    IdAndNameOnlyProjection getAuthorizationType();

    @JsonIgnore
    IdAndNameOnlyProjection getMedicalCenter();

    @JsonIgnore
    IdAndNameOnlyProjection getCompany();

    IdAndNameOnlyProjection getStatus();

    IdAndNameOnlyProjection getAuthorizationCondition();

    @JsonIgnore
    BaseProjection<Long> getBatch();

    @JsonIgnore
    BeneficiaryProjection.Minor getBeneficiary();

    @JsonIgnore
    PractitionerProjection.Minor getPractitioner();

    @JsonIgnore
    PractitionerProjection.Minor getPetitioner();

    @JsonIgnore
    Set<MedicalAuthorizationItemProjection> getMedicalAuthorizationItems();

    BigDecimal getChargeTotal();

    IdAndNameOnlyProjection getCity();

    IdAndNameOnlyProjection getPaymentMethod();

    Boolean getAudited();

    PreMedicalAuthorizationProjection.Minor getPreMedicalAuthorization();

    @JsonIgnore
    Set<PrescriptionProjection.Minor> getPrescriptions();

    Set<MedicalAuthorizationFailureProjection> getFailures();

    RatingProjection getRating();

    Boolean getRefundableItems();

    interface Status extends BaseProjection<Long> {
        IdAndNameOnlyProjection getStatus();

        Set<MedicalAuthorizationItemProjection.Status> getMedicalAuthorizationItems();

        LocalDateTime getModifiedAt();
    }

    interface IdCreationDateBeneficiary extends BaseProjection<Long> {

        LocalDateTime getCreatedAt();

        BeneficiaryProjection.Minor getBeneficiary();

    }

    @JsonSerialize(using = MedicalAuthorizationProjectionQRSerializer.class)
    interface QR {
        BeneficiaryProjection getBeneficiary();

        @Value("#{target.getPreMedicalAuthorization() != null ? target.getPreMedicalAuthorization().getCode() : null}")
        String getCode();

        PersonProjection getPetitioner();

        Set<MedicalAuthorizationItemProjection.QR> getMedicalAuthorizationItems();
    }

    interface Diagnosis {
        IdAndNameOnlyProjection getDisease();

        String getDiagnosis();
    }

    interface IdStatusAndCreationDate extends BaseProjection<Long> {
        LocalDateTime getCreatedAt();

        IdAndNameOnlyProjection getStatus();
    }

}
