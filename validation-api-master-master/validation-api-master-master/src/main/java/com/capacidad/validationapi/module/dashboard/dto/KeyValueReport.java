package com.capacidad.validationapi.module.dashboard.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class KeyValueReport {

    private Object key;
    private Object value;

    public KeyValueReport(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

}
