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

//import org.springframework.security.oauth2.provider.ClientDetails;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COMA;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID;

/*
 * Su responsabilidad es manejar la validación y selección del tenant (inquilino, es decir, el contexto multi-tenant del sistema).
 *
 *
 *  Permite buscar un tenant por ID.
    Valida el tenant en función del usuario, el cliente OAuth2 y el encabezado de la petición.
    Establece el tenant en el contexto (TenantContext), para que todoo el flujo trabaje en ese tenant.

    * Controla errores comunes:
    Tenant no encontrado.
    Usuario con múltiples tenants pero sin especificar cuál.
    Cliente OAuth2 intentando usar un tenant que no le corresponde
 *
 *
 * */


@Service
public class TenantServiceImpl extends BaseServiceImpl<Tenant, Long> implements TenantService {

    private final TenantRepository tenantRepository;

    @Autowired
    public TenantServiceImpl(TenantRepository repository) {
        super(repository);
        this.tenantRepository = repository;
    }

    // obtiene un tenant concreto para operar con él
    @Override
    public Tenant findTenantById(UUID tenantId) throws ObjectNotFoundException {
        return tenantRepository
                .findByTenantIdAndDeletedIsFalse(tenantId) // busca un tenant que no este eliminado
                .orElseThrow(() -> new ObjectNotFoundException("tenant.notFoundTenantId", tenantId.toString()));
    }


    // Sirve para validar y establecer el tenant actual del usuario autenticado.
    @Override
    public ApplicationUserContext validateTenant(ApplicationUser user, RegisteredClient registeredClient, String tenantHeader) {

        // Si el usuario (ApplicationUser) tiene más de un contexto (varios tenants asociados):
        if (user.getContextSet().size() > 1) {

            // Si no vino tenantHeader → lanza UnspecifiedTenantException, porque el sistema no sabe cuál usar.
            //En el mensaje de error concatena todos los tenants del usuario, para que el cliente pueda elegir.
            if (StringUtils.isBlank(tenantHeader)) {
                String tenants = user.getContextSet().stream()
                        .map(context -> Utils.buildTenantResponse(context.getTenant()))
                        .collect(Collectors.joining(COMA));

                throw new UnspecifiedTenantException(tenants);

            } else {
                //Si vino tenantHeader → busca el contexto correspondiente.
                //Si existe → llama a validateAndSetTenant y lo retorna.
                //Si no existe → lanza error.
                ApplicationUserContext context = user.getContextSet().stream()
                        .filter(userContext -> StringUtils.equals(userContext.getTenant().getTenantId().toString(),
                                tenantHeader))
                        .findFirst()
                        .orElse(null);
                validateAndSetTenant(context, registeredClient);
                return context;
            }
        } else {

            //Si el usuario solo tiene un contexto (un único tenant):
            //Lo toma directamente, lo valida y lo asigna
            ApplicationUserContext context = user.getContextSet().iterator().next();
            validateAndSetTenant(context, registeredClient);
            return context;
        }
    }


    private void validateAndSetTenant(ApplicationUserContext context, RegisteredClient registeredClient) {

        // Revisa que el contexto no sea null.
        if (context == null)
            throw new InsufficientAuthenticationException("tenant.invalidTenantUser");

        //Valida que el cliente OAuth2 (RegisteredClient) tenga permiso para operar con ese tenant (validateClientTenant).
        Tenant contextTenant = context.getTenant();
        validateClientTenant(registeredClient, contextTenant);

        //Si todoo está bien, setea el tenant actual en un ThreadLocal (TenantContext),
        // para que el resto de la app sepa con qué tenant se está trabajando.
        TenantContext.setTenant(contextTenant);
    }


    private void validateClientTenant(RegisteredClient registeredClient, Tenant tenant) {

        // Obtiene el tenantId asociado al cliente OAuth2 (RegisteredClient).
        String clientTenantId = Utils.getClientAdditionalInformation(TENANT_ID, registeredClient).orElse("");

        //Si el cliente tiene un tenantId configurado y no coincide con el del contexto del usuario,
        // lanza un error → el cliente no puede operar en ese tenant.
        if (StringUtils.isNotBlank(clientTenantId) && !StringUtils.equals(clientTenantId, tenant.getTenantId().toString()))
            throw new InsufficientAuthenticationException("tenant.clientCannotOperate");
    }

}
