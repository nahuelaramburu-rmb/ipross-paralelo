package com.capacidad.identityservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordDTO {

    @NotEmpty
    private String username;

    @NotEmpty
    @JsonProperty("new_password")
    private String newPassword;

}
