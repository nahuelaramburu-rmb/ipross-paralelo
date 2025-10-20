package com.capacidad.validationapi.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class Tendency {

    private static final String KEY_ALIAS = "key";
    private static final String CURRENT_VALUE_ALIAS = "currentValue";
    private static final String PREVIOUS_VALUE_ALIAS = "previousValue";
    @JsonIgnore
    private Object key;
    private BigDecimal currentValue;
    private BigDecimal previousValue;

    public Tendency(Long currentValue, Long previousValue) {
        this.currentValue = currentValue != null ? new BigDecimal(currentValue) : new BigDecimal(0);
        this.previousValue = previousValue != null ? new BigDecimal(previousValue) : new BigDecimal(0);
    }

    public Tendency(Object key, Long currentValue, Long previousValue) {
        this.key = key;
        this.currentValue = currentValue != null ? new BigDecimal(currentValue) : new BigDecimal(0);
        this.previousValue = previousValue != null ? new BigDecimal(previousValue) : new BigDecimal(0);
    }

    public Tendency(BigDecimal currentValue, BigDecimal previousValue) {
        this.currentValue = currentValue != null ? currentValue : new BigDecimal(0);
        this.previousValue = previousValue != null ? previousValue : new BigDecimal(0);
    }

    public Tendency(Object key, BigDecimal currentValue, BigDecimal previousValue) {
        this.key = key;
        this.currentValue = currentValue != null ? currentValue : new BigDecimal(0);
        this.previousValue = previousValue != null ? previousValue : new BigDecimal(0);
    }

}
