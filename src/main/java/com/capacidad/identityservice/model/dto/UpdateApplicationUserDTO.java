package com.capacidad.identityservice.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class UpdateApplicationUserDTO {

    @NotBlank
    @Email
    private String email;

    @NotNull
    @Valid
    private ProfileDTO profile;

}
