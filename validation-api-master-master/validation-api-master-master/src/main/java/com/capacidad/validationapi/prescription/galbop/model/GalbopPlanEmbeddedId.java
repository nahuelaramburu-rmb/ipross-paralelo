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
public class GalbopPlanEmbeddedId implements Serializable {

    @Column(name = "IDObSoc")
    private Long funderId;

    @Column(name = "IDPlan")
    private Integer planId;

    public GalbopPlanEmbeddedId(long funderId, int planId) {
        this.funderId = funderId;
        this.planId = planId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalbopPlanEmbeddedId that = (GalbopPlanEmbeddedId) o;
        return funderId.equals(that.getFunderId()) &&
                planId.equals(that.getPlanId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(funderId, planId);
    }

}
