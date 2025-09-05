package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class RestorePasswordDTO extends BaseDTO<Long> {

    @NotEmpty
    @Email
    private String email;

    @NotNull
    @Positive
    @JsonProperty("restore_otp")
    private Integer restoreOtp;

    @NotEmpty
    @JsonProperty("new_password")
    private String newPassword;

}
