package com.capacidad.validationapi.prescription.integration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class ProductWrapperDTO {

    Set<ProductIntegrationProjection> products;
    int planId;

    public ProductWrapperDTO(Set<ProductIntegrationProjection> products, int planId) {
        this.products = products;
        this.planId = planId;
    }

}
