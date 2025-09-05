package com.capacidad.validationapi.module.beneficiary.reference;

import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;

public enum PaymentMethodReference {
    PAYCHECK(1L),
    VOLUNTARY(2L);

    private final long id;

    PaymentMethodReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public PaymentMethod getInstance() {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(id);
        return paymentMethod;
    }

}
