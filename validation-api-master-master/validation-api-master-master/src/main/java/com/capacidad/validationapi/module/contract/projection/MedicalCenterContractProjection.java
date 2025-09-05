package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface MedicalCenterContractProjection extends ContractProjection {

    @JsonIgnore
    IdAndNameOnlyProjection getMedicalCenter();

}
