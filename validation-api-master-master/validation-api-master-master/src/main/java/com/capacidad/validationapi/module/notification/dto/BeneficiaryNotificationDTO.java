package com.capacidad.validationapi.module.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryNotificationDTO extends NotificationDTO {

    @NotBlank
    private String beneficiaryCode;

    @NotNull
    private boolean notifyFamilyGroup;

    private Map<String, Object> extraData;

}
