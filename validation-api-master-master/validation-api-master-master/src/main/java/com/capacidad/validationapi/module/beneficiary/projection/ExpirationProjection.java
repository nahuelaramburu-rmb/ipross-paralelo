package com.capacidad.validationapi.module.beneficiary.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;

import java.time.LocalDateTime;

public interface ExpirationProjection extends BaseProjection<Long> {

    LocalDateTime getExpirationDate();

    String getReason();

    interface BeneficiaryId {
        BaseProjection<Long> getBeneficiary();
    }

}
