package com.capacidad.validationapi.module.medicalauthorization.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.batch.projection.BatchItemProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageItemProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorConfigProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public interface MedicalAuthorizationItemProjection extends BaseProjection<Long> {

    IdAndNameOnlyProjection getStatus();

    BigDecimal getChargeSubtotal();

    BigDecimal getChargeUnitPrice();

    @JsonIgnore
    EmbeddedNomenclator getNomenclator();

    Integer getQuantity();

    Boolean getSettled();

    LocalDateTime getCreatedAt();

    String getResolution();

    Set<MedicalAuthorizationFailureProjection> getFailures();

    IdAndNameOnlyProjection getAuthorizationCondition();

    @JsonIgnore
    MedicalCoverageItemProjection.Parent getMedicalCoverageItem();

    BigDecimal getUnitPrice();

    BigDecimal getSubtotal();

    Boolean getRefundable();

    @JsonIgnore
    BatchItemProjection.BatchId getBatchItem();

    CalendarEventType getCalendarEventType();

    interface WithParent extends MedicalAuthorizationItemProjection {
        EmbeddedMedicalAuthorization getMedicalAuthorization();
    }

    interface EmbeddedMedicalAuthorization extends BaseProjection<Long> {
        LocalDateTime getCreatedAt();

        IdAndNameOnlyProjection getMedicalCenter();

        IdAndNameOnlyProjection getContract();

        BeneficiaryProjection.Minor getBeneficiary();
    }

    interface EmbeddedNomenclator extends BaseProjection<Long> {
        Long getNomenclatorCode();

        IdAndNameOnlyProjection getMedicalPractice();

        NomenclatorConfigProjection getNomenclatorConfig();
    }

    interface Status extends BaseProjection<Long> {
        IdAndNameOnlyProjection getStatus();

        String getResolution();

        Integer getQuantity();
    }

    interface QR {
        NomenclatorProjection.Minor getNomenclator();

        Integer getQuantity();
    }

}
