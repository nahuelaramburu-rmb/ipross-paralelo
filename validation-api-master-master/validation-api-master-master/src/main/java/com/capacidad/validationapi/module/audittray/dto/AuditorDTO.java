package com.capacidad.validationapi.module.audittray.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class AuditorDTO extends BaseDTO<Long> {

    @NotNull
    private UUID sub;

    @NotEmpty
    @Size(min = 1, max = 34)
    private String username;

    @NotEmpty
    @Size(min = 1, max = 34)
    private String displayName;

}
