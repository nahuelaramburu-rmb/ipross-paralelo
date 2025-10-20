package com.capacidad.validationapi.module.beneficiary.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.PAYCHECK;

@Entity
@Table(name = "payment_method")
@NoArgsConstructor
@Getter
@Setter
public class PaymentMethod extends BaseEntity<Long> {

    @Column(nullable = false, unique = true)
    private String name;

    public boolean isPaycheck() {
        return this.getId().equals(PAYCHECK.getId());
    }

}
