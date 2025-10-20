package com.capacidad.validationapi.module.beneficiary.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.disease.projection.ICD10DiseaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.person.projection.PersonProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;
import java.util.UUID;

public interface BeneficiaryProjection extends PersonProjection {

    String getBeneficiaryCode();

    IdAndNameOnlyProjection getStatus();

    LocalDateTime getCreatedAt();

    IdAndNameOnlyProjection getRelationshipType();

    IdAndNameOnlyProjection getBeneficiaryCategory();

    IdAndNameOnlyProjection getCompany();

    IdAndNameOnlyProjection getPaymentMethod();

    @JsonIgnore
    BaseProjection<Long> getRelatedBeneficiary();

    @JsonIgnore
    Set<BeneficiaryInsurancePlanProjection> getBeneficiaryInsurancePlans();

    @JsonIgnore
    Set<ICD10DiseaseProjection> getDiseases();

    UUID getResourceId();

    Boolean getActiveBatch();

    @JsonIgnore
    Set<IdAndNameOnlyProjection> getTradeUnions();

    interface Minor extends PersonProjection {

        String getBeneficiaryCode();

        IdAndNameOnlyProjection getRelationshipType();

        IdAndNameOnlyProjection getStatus();

        Boolean getActiveBatch();
    }

    interface Full extends PersonProjection {
        String getBeneficiaryCode();

        IdAndNameOnlyProjection getStatus();

        LocalDateTime getCreatedAt();

        IdAndNameOnlyProjection getRelationshipType();

        IdAndNameOnlyProjection getBeneficiaryCategory();

        IdAndNameOnlyProjection getCompany();

        IdAndNameOnlyProjection getPaymentMethod();

        Set<BeneficiaryInsurancePlanProjection> getBeneficiaryInsurancePlans();

        Set<ICD10DiseaseProjection> getDiseases();

        Boolean getActiveBatch();
    }

    interface Verification extends PersonProjection {

        UUID getResourceId();

        @JsonIgnore
        IdAndNameOnlyProjection getStatus();

        @JsonIgnore
        default int getAge() {
            return Period.between(this.getBirthDate(), LocalDate.now()).getYears();
        }

        @JsonIgnore
        default boolean hasActiveHealthCoverage() {
            return this.getStatus().getId().equals(StatusReference.BENEFICIARY_WITH_COVERAGE.getId());
        }

    }
}
