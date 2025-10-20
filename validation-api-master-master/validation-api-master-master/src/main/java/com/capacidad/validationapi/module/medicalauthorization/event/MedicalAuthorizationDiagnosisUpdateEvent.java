package com.capacidad.validationapi.module.medicalauthorization.event;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalAuthorizationDiagnosisUpdateEvent extends AuditorAwareEvent {

    private final MedicalAuthorization medicalAuthorization;

    public MedicalAuthorizationDiagnosisUpdateEvent(MedicalAuthorization medicalAuthorization, boolean notifyAuditors) {
        super(medicalAuthorization, notifyAuditors);
        this.medicalAuthorization = medicalAuthorization;
    }
}
