package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class NewPasswordDTO extends BaseDTO<Long> {

    @NotEmpty
    private String password;

    @NotEmpty
    @JsonProperty("new_password")
    private String newPassword;

}
