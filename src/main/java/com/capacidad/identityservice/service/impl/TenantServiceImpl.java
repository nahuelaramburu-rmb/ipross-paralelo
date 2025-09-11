package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.exception.UnspecifiedTenantException;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.repository.TenantRepository;
import com.capacidad.identityservice.service.TenantService;
import com.capacidad.identityservice.service.base.BaseServiceImpl;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID;

@Service
public class TenantServiceImpl extends BaseServiceImpl<Tenant, Long> implements TenantService {

    private final TenantRepository tenantRepository;

    @Autowired
    public TenantServiceImpl(TenantRepository repository) {
        super(repository);
        this.tenantRepository = repository;
    }

    @Override
    public Tenant findTenantById(UUID tenantId) throws ObjectNotFoundException {
        return tenantRepository
                .findByTenantIdAndDeletedIsFalse(tenantId)
                .orElseThrow(() -> new ObjectNotFoundException("tenant.notFoundTenantId", tenantId.toString()));
    }

    @Override
    public ApplicationUserContext validateTenant(ApplicationUser user, ClientDetails clientDetails, String tenantHeader) {
        if (user.getContextSet().size() > 1) {
            if (StringUtils.isBlank(tenantHeader)) {
                String tenants = user.getContextSet().stream()
                        .map(context -> Utils.buildTenantResponse(context.getTenant()))
                        .collect(Collectors.joining(COMA));
                throw new UnspecifiedTenantException(tenants);
            } else {
                ApplicationUserContext context = user.getContextSet().stream()
                        .filter(userContext -> StringUtils.equals(userContext.getTenant().getTenantId().toString(),
                                tenantHeader))
                        .findFirst()
                        .orElse(null);
                validateAndSetTenant(context, clientDetails);
                return context;
            }
        } else {
            ApplicationUserContext context = user.getContextSet().iterator().next();
            validateAndSetTenant(context, clientDetails);
            return context;
        }
    }

    private void validateAndSetTenant(ApplicationUserContext context, ClientDetails clientDetails) {
        if (context == null)
            throw new InsufficientAuthenticationException("tenant.invalidTenantUser");
        Tenant contextTenant = context.getTenant();
        validateClientTenant(clientDetails, contextTenant);
        TenantContext.setTenant(contextTenant);
    }

    private void validateClientTenant(ClientDetails clientDetails, Tenant tenant) {
        String clientTenantId = Utils.getClientAdditionalInformation(TENANT_ID, clientDetails).orElse("");
        if (StringUtils.isNotBlank(clientTenantId) && !StringUtils.equals(clientTenantId, tenant.getTenantId().toString()))
            throw new InsufficientAuthenticationException("tenant.clientCannotOperate");
    }

}
