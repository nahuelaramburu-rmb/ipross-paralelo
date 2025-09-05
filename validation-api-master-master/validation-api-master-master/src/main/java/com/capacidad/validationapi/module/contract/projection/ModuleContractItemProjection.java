package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;

public interface ModuleContractItemProjection extends ContractItemProjection {

    IdAndNameOnlyProjection getModule();

}
