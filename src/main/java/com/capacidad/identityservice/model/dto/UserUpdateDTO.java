package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class UserUpdateDTO extends BaseDTO<Long> {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @JsonProperty("new_password")
    @NotBlank
    private String newPassword;

}
