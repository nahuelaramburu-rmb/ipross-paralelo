package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class GalbopPrescriptionEmbeddedId implements Serializable {

    @Column(name = "IDPreAutorizacion")
    private Long prescriptionId;

    @Column(name = "IDPlan")
    private Long planId;

    @Column(name = "IDBeneficiario")
    private String beneficiaryTypeId;

    @Column(name = "IDAfiliado")
    private String beneficiaryCode;

    public GalbopPrescriptionEmbeddedId(long prescriptionId, long planId, String beneficiaryTypeId, String beneficiaryCode) {
        this.prescriptionId = prescriptionId;
        this.planId = planId;
        this.beneficiaryTypeId = beneficiaryTypeId;
        this.beneficiaryCode = beneficiaryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalbopPrescriptionEmbeddedId that = (GalbopPrescriptionEmbeddedId) o;
        return prescriptionId.equals(that.getPlanId()) &&
                planId.equals(that.getPlanId()) &&
                beneficiaryTypeId.equals(that.getBeneficiaryTypeId()) &&
                beneficiaryCode.equals(that.getBeneficiaryCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescriptionId,
                planId,
                beneficiaryTypeId,
                beneficiaryCode);
    }

}
