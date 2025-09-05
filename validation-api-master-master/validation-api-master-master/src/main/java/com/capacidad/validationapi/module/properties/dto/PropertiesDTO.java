package com.capacidad.validationapi.module.properties.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.general.model.Period;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class PropertiesDTO extends BaseDTO<Long> {

    @NotNull
    @Positive
    private Integer preAuthorizationMaxDays;

    @NotNull
    private String prescriptionService;

    @NotNull
    private Period prescriptionExpirationPeriod;

    private Map<String, Object> mappings;

}
