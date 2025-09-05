package com.capacidad.validationapi.module.company.projection;

import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.projection.AddressProjection;

public interface CompanyProjection extends BaseProjection<Long>, IdAndNameOnlyProjection {

    AddressProjection getAddress();

}
