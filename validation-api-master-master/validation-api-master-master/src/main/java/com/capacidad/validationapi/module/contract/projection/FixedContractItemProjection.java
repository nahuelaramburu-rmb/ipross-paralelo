package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public interface FixedContractItemProjection extends ContractItemProjection {

    NomenclatorProjection.Minor getNomenclator();

    @JsonIgnore
    Set<ContractItemSpecialPriceProjection> getSpecialPrices();

}
