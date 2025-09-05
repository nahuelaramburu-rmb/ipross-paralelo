package com.capacidad.validationapi.module.location.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.dto.RegionDTO;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.projection.CountryProjection;
import com.capacidad.validationapi.module.location.projection.RegionProjection;

import java.util.List;
import java.util.Set;

public interface RegionService extends BaseService<Region, RegionDTO, Long> {

    Set<RegionProjection> getRegions(String name);

    List<CountryProjection> getAllCountries();

    List<IdAndNameOnlyProjection> getProvinces(long countryId) throws ObjectNotFoundException;

    List<IdAndNameOnlyProjection> getCities(long provinceId) throws ObjectNotFoundException;

    boolean cityBelongToRegion(Region region, City city);

}
