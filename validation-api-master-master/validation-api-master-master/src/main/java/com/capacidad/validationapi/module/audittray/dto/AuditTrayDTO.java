package com.capacidad.validationapi.module.audittray.dto;

import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class AuditTrayDTO extends BaseDTO<Long> {

    @NotBlank
    @Size(min = 1, max = 35)
    private String name;

    @NotBlank
    @Size(min = 1, max = 200)
    private String purpose;

    @NotEmpty
    @Valid
    private Set<AuditorDTO> auditors;

    @NotEmpty
    @Valid
    private Set<IdDTO<Long>> nomenclators;

    @Valid
    private IdDTO<Long> region;

    @Valid
    private IdDTO<Long> city;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    @Size(min = 4, max = 7)
    @NotBlank
    private String color;

}
