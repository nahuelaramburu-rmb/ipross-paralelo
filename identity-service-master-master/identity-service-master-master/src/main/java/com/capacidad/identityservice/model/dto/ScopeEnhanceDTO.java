package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ScopeEnhanceDTO extends BaseDTO<Long> {

    @NotEmpty
    List<String> roles;

    @NotEmpty
    String resource;

}
