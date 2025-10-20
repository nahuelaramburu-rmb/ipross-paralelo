package com.capacidad.validationapi.prescription.galbop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class GalbopBeneficiaryEmbeddedId implements Serializable {

    @Column(name = "IDPadron")
    private Long funderId;

    @Column(name = "IDAfiliado")
    private Long beneficiaryCode;

    @Column(name = "IDBeneficiario")
    private Long beneficiaryTypeId;

    @Column(name = "IDPlan")
    private Long planId;

    @Column(name = "FechaAlta")
    private LocalDate createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalbopBeneficiaryEmbeddedId that = (GalbopBeneficiaryEmbeddedId) o;
        return funderId.equals(that.getFunderId()) &&
                beneficiaryCode.equals(that.getBeneficiaryCode()) &&
                beneficiaryTypeId.equals(that.getBeneficiaryTypeId()) &&
                planId.equals(that.getPlanId()) &&
                createdAt.equals(that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(funderId, beneficiaryCode, beneficiaryTypeId, planId, createdAt);
    }


}
