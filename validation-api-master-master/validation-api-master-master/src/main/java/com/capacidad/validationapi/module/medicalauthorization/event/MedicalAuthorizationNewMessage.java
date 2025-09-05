package com.capacidad.validationapi.module.medicalauthorization.event;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MedicalAuthorizationNewMessage extends AuditorAwareEvent {

    private final MedicalAuthorization medicalAuthorization;
    private final List<String> resourceIds;

    public MedicalAuthorizationNewMessage(MedicalAuthorization medicalAuthorization, boolean notifyAuditors, List<String> resourceIds) {
        super(medicalAuthorization, notifyAuditors);
        this.medicalAuthorization = medicalAuthorization;
        this.resourceIds = resourceIds;
    }

}
