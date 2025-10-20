package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PractitionerContractProjection extends ContractProjection {

    @JsonIgnore
    PractitionerProjection.Minor getPractitioner();

}
