package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class GalbopPrescriptionDetailEmbeddedId implements Serializable {

    @Column(name = "IDPreAutorizacion")
    private Long prescriptionId;

    @Column(name = "Receta")
    private Integer prescriptionValue;

    public GalbopPrescriptionDetailEmbeddedId(long prescriptionId, int prescriptionValue) {
        this.prescriptionId = prescriptionId;
        this.prescriptionValue = prescriptionValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalbopPrescriptionDetailEmbeddedId that = (GalbopPrescriptionDetailEmbeddedId) o;
        return prescriptionId.equals(that.getPrescriptionId()) &&
                prescriptionValue.equals(that.getPrescriptionValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescriptionId, prescriptionValue);
    }

}
