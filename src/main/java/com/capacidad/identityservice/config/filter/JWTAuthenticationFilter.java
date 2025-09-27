package com.capacidad.identityservice.config.filter;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.config.token.TokenVerifier;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.misc.securityutils.SecurityUtils;
import com.capacidad.identityservice.model.Group;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.service.TenantService;

import com.capacidad.utils.TokenUtils;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.identityservice.misc.Utils.isNotCustomEndpoint;
import static com.capacidad.identityservice.misc.Utils.sendError;
import static com.capacidad.utils.Constants.*;


/*
 * función principal: interceptar las solicitudes, validar el JWT que viene en los headers,
 * y configurar el contexto de seguridad para que la aplicación sepa quién es el usuario autenticado, su rol, permisos y tenant.
 *
 *
 *  Extrae el JWT del request.
    Lo valida y parsea los claims.

Configura:
Tenant actual (TenantContext)
Usuario autenticado (JWTAuthenticationToken)
Permisos y rol (GrantedAuthority)
Coloca todo esto en el SecurityContext para que el resto de la app sepa quién es el usuario y qué puede hacer.
Maneja errores de autenticación y asegura la limpieza del contexto al final de cada request.
 *
 * */

//@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    //verifica que el token sea legítimo y confiable antes de procesarlo
    private final TokenVerifier tokenVerifier;

    //
    private final TenantService tenantService;

    //
    private final GlobalExceptionHandler exceptionHandler;

    @Autowired
    public JWTAuthenticationFilter(TenantService tenantService,
                                   TokenVerifier tokenVerifier,
                                   GlobalExceptionHandler exceptionHandler) {
        this.tenantService = tenantService;
        this.tokenVerifier = tokenVerifier;
        this.exceptionHandler = exceptionHandler;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        //Obtiene el token JWT del request
        // lee el Authorization: Bearer <token> del header
        Optional<String> bearerToken = SecurityUtils.getBearerToken(request);


        // Si no hay token o si la URL no es un endpoint que requiera autenticación (isNotCustomEndpoint(request)), deja pasar la petición sin hacer nada (chain.doFilter).
        if (bearerToken.isEmpty() || isNotCustomEndpoint(request)) {
            chain.doFilter(request, response);
            return;
        }

        // Valida y procesa el JWT:
        //Llama a getAuthentication(bearerToken) para crear un objeto de autenticación.
        //Coloca este objeto en el SecurityContextHolder → de esta manera, Spring sabe qué usuario está autenticado en esta petición.
        try {
            Authentication authentication = getAuthentication(bearerToken.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);

        //Manejo de excepciones:
        // Si ocurre algún error (token inválido, tenant inexistente, etc.), lo resuelve con el GlobalExceptionHandler y responde al cliente con un error HTTP adecuado.
        } catch (Exception e) {
            Utils.resolveException(exceptionHandler, request, e)
                    .ifPresent(throwingConsumer(r -> sendError(response, r))); //se envía el error al cliente

            //Al terminar la petición (éxito o error), limpia el contexto:
            //TenantContext.clearContext() → borra el tenant de la petición.
            //SecurityContextHolder.clearContext() → borra la autenticación.
        } finally {
            TenantContext.clearContext();
            SecurityContextHolder.clearContext();
        }
    }



    private Authentication getAuthentication(String jwt) throws ObjectNotFoundException {

        //Valida el token → tokenVerifier.validate(jwt)
        //Verifica que no esté expirado, manipulado o inválido.
        tokenVerifier.validate(jwt);

        //Parsea los claims → TokenUtils.parseJwt(jwt)
        //Obtiene la info dentro del JWT (usuario, rol, tenant, permisos, etc.).
        Map<String, Object> jwtValues = TokenUtils.parseJwt(jwt);

        //Setea el tenant
        setTenant((String) jwtValues.get(JWT_CLAIM_TENANT));

        //Determina el grupo (Group) y el recurso (resourceId)
        String groupString = (String) jwtValues.get(JWT_CLAIM_GROUP);
        Group group = Group.valueOf(groupString.toUpperCase());
        String resourceId = TokenUtils.getJwtResourceIdClaim(jwt).orElse("");
        UUID uuidResourceId = StringUtils.isNotBlank(resourceId) ? UUID.fromString(resourceId) : null;

        //Crea el token de autenticación interno
        //Retorna un JWTAuthenticationToken con:
        //Usuario (username)
        //Autoridades (GrantedAuthority) → scopes + role
        //Grupo (Group)
        //ResourceId (UUID)
        //Esto se coloca en el contexto de Spring Security.
        return new JWTAuthenticationToken(jwtValues.get(JWT_CLAIM_USERNAME), "", getAuthorities(jwtValues), group, uuidResourceId);
    }


    private void setTenant(String tenantId) throws ObjectNotFoundException {

        //Busca el tenant en base al claim tenantId y lo guarda en un ThreadLocal (TenantContext),
        // para que cualquier parte de la aplicación sepa en qué tenant está trabajando.
        Tenant tenant = tenantService.findTenantById(UUID.fromString(tenantId));
        TenantContext.setTenant(tenant);
    }



    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> getAuthorities(Map<String, Object> jwtValues) {

        //Extrae del JWT la lista de permisos (scope) y el rol principal (role).
        List<String> scopeList = (List<String>) jwtValues.get(JWT_CLAIM_SCOPE);

        //Convierte cada uno en un GrantedAuthority, que es lo que Spring usa para autorizar endpoints (con @PreAuthorize, @Secured, etc.).
        List<GrantedAuthority> grantedAuthorities = scopeList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // agrega el rol principal del usuario (extraído del JWT) a la lista de permisos/autorizaciones (GrantedAuthority)
        // que Spring Security usará para controlar el acceso.
        grantedAuthorities.add(new SimpleGrantedAuthority((String) jwtValues.get(JWT_CLAIM_ROLE)));

        return grantedAuthorities;
    }

}
