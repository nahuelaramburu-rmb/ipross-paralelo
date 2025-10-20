package com.capacidad.validationapi.module.rating;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class RatingDTO extends BaseDTO<Long> {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer quality;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer duration;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer waitTime;

    @NotNull
    @Min(0)
    @Max(5)
    private Integer charges;

}
