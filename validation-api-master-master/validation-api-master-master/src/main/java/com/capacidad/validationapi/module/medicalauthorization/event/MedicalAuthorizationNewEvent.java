package com.capacidad.validationapi.module.medicalauthorization.event;

import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class MedicalAuthorizationNewEvent extends ApplicationEvent {

    private final MedicalAuthorization medicalAuthorization;

    public MedicalAuthorizationNewEvent(MedicalAuthorization medicalAuthorization) {
        super(medicalAuthorization);
        this.medicalAuthorization = medicalAuthorization;
    }

}
