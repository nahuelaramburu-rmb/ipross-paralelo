package com.capacidad.validationapi.module.prescription.event;

import com.capacidad.validationapi.module.prescription.model.Prescription;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NewPrescriptionEvent extends ApplicationEvent {

    private Prescription prescription;

    public NewPrescriptionEvent(Prescription prescription) {
        super(prescription);
        this.prescription = prescription;
    }
}
