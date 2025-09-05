package com.capacidad.validationapi.module.person.dto;

import com.capacidad.validationapi.config.annotation.ValidGender;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.person.model.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class PersonDTO extends BaseDTO<Long> {

    @NotBlank
    private String name;

    @NotBlank
    private String lastName;

    @NotNull
    @Positive
    private Long idNumber;

    @NotNull
    @Valid
    private IdDTO<Long> idType;

    @Positive
    private Long workIdNumber;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotNull
    @ValidGender
    private Gender gender;

    @Valid
    private IdDTO<Long> studies;

    @Valid
    private IdDTO<Long> maritalStatus;

    @Valid
    private IdDTO<Long> occupation;

    @Valid
    private PhoneDTO phone;

    @Email
    private String email;

}
