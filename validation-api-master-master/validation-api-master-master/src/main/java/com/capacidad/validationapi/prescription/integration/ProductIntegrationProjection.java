package com.capacidad.validationapi.prescription.integration;

import java.math.BigDecimal;

public interface ProductIntegrationProjection {

    Long getExchangeId();

    String getProduct();

    String getPresentation();

    String getDescription();

    String getRecommendation();

    Integer getUnits();

    BigDecimal getConcentration();

    Long getConcentrationTypeId();

    Long getProductTypeId();

    Long getUnitsTypeId();

    Long getDrugId();

    Long getSizeId();

    BigDecimal getAuthorizedDosage();

}
