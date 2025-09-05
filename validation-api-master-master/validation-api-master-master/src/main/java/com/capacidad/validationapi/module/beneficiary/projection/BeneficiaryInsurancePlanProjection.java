package com.capacidad.validationapi.module.beneficiary.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.insuranceplan.projection.InsurancePlanProjection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.LocalDate;

public interface BeneficiaryInsurancePlanProjection extends BaseProjection<Long> {

    @JsonUnwrapped
    InsurancePlanProjection getInsurancePlan();

    LocalDate getExpirationDate();

    @JsonProperty("beneficiaryInsurancePlanPriority")
    Integer getPriority();

    @JsonProperty("beneficiaryInsurancePlanId")
    @Override
    Long getId();

    interface BeneficiaryId {
        BaseProjection<Long> getBeneficiary();
    }

}
