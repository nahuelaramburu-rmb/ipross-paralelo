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
public class GalbopPrescriptionValidationEmbeddedId implements Serializable {

    @Column(name = "IDPreAutorizacion")
    private Long prescriptionId;

    @Column(name = "Receta")
    private Integer prescriptionValue;

    @Column(name = "Orden")
    private Integer order;

    @Column(name = "SubOrden")
    private Integer subOrder;

    public GalbopPrescriptionValidationEmbeddedId(GalbopPrescriptionLineEmbeddedId prescriptionLineEmbeddedId, int subOrder) {
        this.prescriptionId = prescriptionLineEmbeddedId.getPrescriptionId();
        this.prescriptionValue = prescriptionLineEmbeddedId.getPrescriptionValue();
        this.order = prescriptionLineEmbeddedId.getOrder();
        this.subOrder = subOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalbopPrescriptionValidationEmbeddedId that = (GalbopPrescriptionValidationEmbeddedId) o;
        return prescriptionId.equals(that.getPrescriptionId()) &&
                prescriptionValue.equals(that.getPrescriptionValue()) &&
                order.equals(that.getOrder()) &&
                subOrder.equals(that.getSubOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescriptionId, prescriptionValue, order, subOrder);
    }

}
