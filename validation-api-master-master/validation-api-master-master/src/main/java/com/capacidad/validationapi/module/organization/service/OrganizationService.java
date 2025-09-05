package com.capacidad.validationapi.module.organization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.organization.dto.OrganizationDTO;
import com.capacidad.validationapi.module.organization.model.Organization;
import com.capacidad.validationapi.module.organization.projection.OrganizationProjection;

import java.util.List;
import java.util.Set;

public interface OrganizationService extends BaseService<Organization, OrganizationDTO, Long> {

    Set<OrganizationProjection> findOrganizationsContaining(String name);

    OrganizationProjection getProjectedAuthOrganization() throws ObjectNotFoundException;

    Organization getAuthOrganization() throws ObjectNotFoundException;

    List<Long> getAuthOrganizationCityIds();

}
