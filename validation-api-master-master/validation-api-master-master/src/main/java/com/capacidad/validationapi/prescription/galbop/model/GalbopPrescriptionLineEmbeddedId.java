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
public class GalbopPrescriptionLineEmbeddedId implements Serializable {

    @Column(name = "IDPreAutorizacion")
    private Long prescriptionId;

    @Column(name = "Receta")
    private Integer prescriptionValue;

    @Column(name = "Orden")
    private Integer order;

    public GalbopPrescriptionLineEmbeddedId(GalbopPrescriptionDetailEmbeddedId prescriptionDetailEmbeddedId, int order) {
        this.prescriptionId = prescriptionDetailEmbeddedId.getPrescriptionId();
        this.prescriptionValue = prescriptionDetailEmbeddedId.getPrescriptionValue();
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalbopPrescriptionLineEmbeddedId that = (GalbopPrescriptionLineEmbeddedId) o;
        return prescriptionId.equals(that.getPrescriptionId()) &&
                prescriptionValue.equals(that.getPrescriptionValue()) &&
                order.equals(that.getOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescriptionId, prescriptionValue, order);
    }

}
