package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ApplicationUserDTO extends BaseDTO<Long> {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    @JsonProperty("resource_id")
    private UUID resourceId;

    @NotNull
    @Valid
    private ProfileDTO profile;

}
