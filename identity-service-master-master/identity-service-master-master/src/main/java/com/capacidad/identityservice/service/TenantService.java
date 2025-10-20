package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.base.BaseService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.UUID;

public interface TenantService extends BaseService<Tenant, Long> {

    Tenant findTenantById(UUID tenantId) throws ObjectNotFoundException;

    ApplicationUserContext validateTenant(ApplicationUser user, ClientDetails clientDetails, String tenantHeader);

}
