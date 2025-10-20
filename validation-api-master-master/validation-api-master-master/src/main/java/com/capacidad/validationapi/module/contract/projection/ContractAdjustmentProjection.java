package com.capacidad.validationapi.module.contract.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.contract.model.ContractAdjustmentScope;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.projection.AddressProjection;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;

import java.time.LocalDateTime;

public interface ContractAdjustmentProjection extends BaseProjection<Long> {

    IdAndNameOnlyProjection getRegion();

    AddressProjection.CityProjection getCity();

    NomenclatorProjection.Minor getNomenclator();

    Period getPeriod();

    LocalDateTime getCreatedAt();

    IdAndNameOnlyProjection getRestrictionType();

    ContractAdjustmentScope getScope();

    String getType();

}
