package com.capacidad.identityservice.config.filter;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.ClientAuthenticationToken;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.misc.securityutils.SecurityUtils;
import com.capacidad.identityservice.model.Group;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.TenantService;


import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.Utils.isNotCustomEndpoint;
import static com.capacidad.identityservice.misc.Utils.sendError;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ROLE_CLIENT_AUTHORITY;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID;
import static com.capacidad.utils.Constants.JWT_CLAIM_GROUP;

// TODO , SE DEBEN TESTEAR ESTOS MÉTODOS , YA QUE AL USAR RegisteredClient , SE DEBEN CAMBIAR DEPENDENCIAS E IMPLEMENTACIONES !!


/*
* Es un filtro de seguridad de Spring Security (OncePerRequestFilter) que intercepta cada request HTTP.
* Su propósito es validar credenciales básicas (Basic Auth) de clientes registrados (RegisteredClient),
* asignarles un contexto de tenant (multi-tenant) y establecer la autenticación en el SecurityContextHolder
*
*
* El filtro:

Intercepta la request.
Valida credenciales básicas de un cliente OAuth2 registrado.
Determina su tenant y lo carga en el contexto.
Determina su grupo lógico (DEV/PROD/TEST/UAT).
Asigna sus roles/autorizaciones.
Coloca la autenticación en SecurityContextHolder.
Si falla, devuelve un error manejado.
Al final limpia el contexto para no afectar a otras requests.
*
*
*
* */


@Component
public class ClientBasicAuthenticationFilter extends OncePerRequestFilter {

    private final GlobalExceptionHandler exceptionHandler;
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantService tenantService;

    @Autowired
    public ClientBasicAuthenticationFilter(GlobalExceptionHandler exceptionHandler,
                                           RegisteredClientRepository registeredClientRepository,
                                           PasswordEncoder passwordEncoder,
                                           TenantService tenantService) {
        this.exceptionHandler = exceptionHandler;
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantService = tenantService;
    }


    // Extrae credenciales básicas (id , password del cliente)
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        // obtiene [clientId, clientSecret]
        Optional<String[]> basicAuthCredentials = SecurityUtils.getBasicAuthorizationCredentials(request);

        //Si no hay credenciales o no es un endpoint que requiera este filtro (isNotCustomEndpoint(request)),
        // deja pasar la petición (chain.doFilter) sin autenticación especial.
        if (basicAuthCredentials.isEmpty() || isNotCustomEndpoint(request)) {
            chain.doFilter(request, response);
            return;
        }

        // si hay credenciales
        try {

            // valida las credenciales
            Authentication authentication = getAuthentication(basicAuthCredentials.get());

            // si la autenticacion es exitosa , la guarda en el contexto
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // se pasa al siguiente filtro
            chain.doFilter(request, response);

            //Si el tenant o el cliente no existe
        } catch (ObjectNotFoundException e) {
            sendError(response, exceptionHandler.handleObjectNotFoundException(request, e));

            // Si las credenciales son inválidas
        } catch (AuthenticationException e) {
            sendError(response, exceptionHandler.handleAuthenticationException(request, e));

            // se limpia el context
        } finally {
            TenantContext.clearContext();
            SecurityContextHolder.clearContext();
        }
    }


    private Authentication getAuthentication(String[] basicAuthCredentials) throws ObjectNotFoundException {

        try {
            // se obtiene el cliente en db , en base a su id
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(basicAuthCredentials[0]);

            // assert null
            assert registeredClient != null;

            // se setea el cliente en el context (para determinar el Tenant asociado al cliente y ponerlo en el TenantContext.)
            setTenantContext(registeredClient);

            // se valida el password del cliente
            if (!passwordEncoder.matches(basicAuthCredentials[1], registeredClient.getClientSecret())) {

                throw new BadCredentialsException("Invalid Client Credentials");
            }

            /*
             Revisa la información adicional del cliente para obtener el Group (DEV, PROD, TEST, UAT).
            (sirve para clasificar al cliente en un entorno lógico)
            luego, el ClientAuthenticationToken se crea con ese grupo, lo que significa que el sistema sabe en qué entorno o categoría pertenece ese cliente.
            * */
            Optional<String> groupString = Utils.getClientAdditionalInformation(JWT_CLAIM_GROUP, registeredClient);

            // Group : DEV, PROD, TEST, UAT
            Group clientGroup = groupString.map(gp -> Group.valueOf(gp.toUpperCase())).orElse(null);

            // crea un nuevo client token
            return new ClientAuthenticationToken(registeredClient.getClientId(),
                    "",
                    getClientAuthorities(registeredClient),
                    clientGroup);

            // no existe esta expception
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    //    obtiene las authorities , en base al rol del cliente
    //Esto define qué permisos tendrá el cliente en el sistema
    private Collection<? extends GrantedAuthority> getClientAuthorities(RegisteredClient registeredClient) {

        //Convierte los scopes del cliente en authorities (SimpleGrantedAuthority).
        //scopes-> permisos declarados en OAuth2 que definen qué puede hacer el cliente.
        List<GrantedAuthority> authorities = registeredClient.getScopes().stream()
                .map(SimpleGrantedAuthority::new) //adaptación de scopes a authorities para que Spring Security pueda gestionarlos con su modelo estándar de autorización
                .collect(Collectors.toList());

        //agrega una autoridad extra fija (ROLE_CLIENT_AUTHORITY).
        authorities.add(ROLE_CLIENT_AUTHORITY);

        return authorities;
    }


    //Esto permite trabajar en un esquema multi-tenant (donde cada request sabe a qué tenant pertenece el cliente autenticado).
    private void setTenantContext(RegisteredClient registeredClient) throws ObjectNotFoundException {

        // TODO ,se debe validar que este metodo obtiene un tenantId,
        // TODO , ya que se debe setear en el momento de crear el user !!
        // TODO , PARA ESTO , SE DEBE REIMPLEMENTAR LA CLASE  "OAuthServerConfig"

        //Obtiene el tenantId de la información adicional del cliente.
        String tenantId = Utils.getClientAdditionalInformation(TENANT_ID, registeredClient).orElse("");

        //si existe
        if (StringUtils.isNotBlank(tenantId)) {

            // obtiene el tenant del cliente en db
            Tenant tenant = tenantService.findTenantById(UUID.fromString(tenantId));

            //Lo asigna al contexto actual
            //Sin el TenantContext, no se podría diferenciar a qué tenant corresponde un request.
            TenantContext.setTenant(tenant);
        }
    }

}
