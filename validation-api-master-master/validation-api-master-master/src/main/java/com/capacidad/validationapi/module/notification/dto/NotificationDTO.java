package com.capacidad.validationapi.module.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class NotificationDTO {

    @Size(min = 1, max = 40)
    @NotBlank
    private String title;

    @Size(min = 1, max = 80)
    private String body;

}
