package com.capacidad.validationapi.module.medicine.projection;

import java.math.BigDecimal;

public interface MedicineProjection {

    String getProduct();

    String getPresentation();

    Integer getUnits();

    BigDecimal getConcentration();

    interface Integration {

        Long getExchangeId();

        Long getExchangePlanId();

        String getProduct();

        String getRecommendation();

        String getPresentation();

        Integer getUnits();

        BigDecimal getConcentration();

        Long getConcentrationTypeId();

        Long getProductTypeId();

        Long getUnitsTypeId();

        Long getDrugId();

        BigDecimal getAuthorizedDosage();

    }

}
