package com.capacidad.identityservice.model.dto;

import com.capacidad.identityservice.model.dto.base.BaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@NoArgsConstructor
@Getter
@Setter
public class ProfileDTO extends BaseDTO<Long> {

    @NotBlank
    private String name;

    @NotBlank
    @JsonProperty("last_name")
    private String lastName;
        
    @Positive
    @NotNull
    @JsonProperty("id_number")
    private Long idNumber;

    @NotBlank
    @JsonProperty("id_type")
    private String idType;
}
