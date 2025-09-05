package com.capacidad.validationapi.module.rating;

import java.math.BigDecimal;

public interface RatingProjection {

    Integer getQuantity();

    BigDecimal getAverage();

    BigDecimal getQuality();

    BigDecimal getDuration();

    BigDecimal getCharges();

    BigDecimal getWaitTime();

}
