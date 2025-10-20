package com.capacidad.identityservice.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
