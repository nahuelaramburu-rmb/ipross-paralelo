package com.capacidad.validationapi.module.medicalauthorization.event;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalAuthorizationStatusUpdateEvent extends AuditorAwareEvent {

    private final MedicalAuthorization medicalAuthorization;

    public MedicalAuthorizationStatusUpdateEvent(MedicalAuthorization medicalAuthorization, boolean notifyAuditors) {
        super(medicalAuthorization, notifyAuditors);
        this.medicalAuthorization = medicalAuthorization;
    }

}
