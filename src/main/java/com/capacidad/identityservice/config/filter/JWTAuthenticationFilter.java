package com.capacidad.identityservice.config.filter;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.config.security.JwtService;
import com.capacidad.identityservice.config.token.TokenVerifier;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.misc.securityutils.SecurityUtils;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.model.Group;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.repository.TokenRepository;
import com.capacidad.identityservice.service.TenantService;

import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import com.capacidad.utils.TokenUtils;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

   // private final TenantService tenantService;
   // private final GlobalExceptionHandler exceptionHandler;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenRepository tokenRepository;

    @Autowired
    public JWTAuthenticationFilter(TenantService tenantService,
                                   GlobalExceptionHandler exceptionHandler,
                                   JwtService jwtService,
                                   CustomUserDetailsService customUserDetailsService,
                                   TokenRepository tokenRepository) {
       // this.tenantService = tenantService;
       // this.exceptionHandler = exceptionHandler;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenRepository = tokenRepository;
    }


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (request.getServletPath().contains("/identity-service/v1/auth/**")) {

            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ver aca
            CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            var isTokenValid = tokenRepository.findByToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);

            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }


//    private Authentication getAuthentication(String jwt,CustomUserDetailsService customUserDetailsService) throws ObjectNotFoundException {
//
//        //Valida el token → tokenVerifier.validate(jwt)
//        //Verifica que no esté expirado, manipulado o inválido.
//        jwtService.isTokenValid(jwt,customUserDetailsService);
//
//        //Parsea los claims → TokenUtils.parseJwt(jwt)
//        //Obtiene la info dentro del JWT (usuario, rol, tenant, permisos, etc.).
//        Map<String, Object> jwtValues = TokenUtils.parseJwt(jwt);
//
//        //Setea el tenant
//        setTenant((String) jwtValues.get(JWT_CLAIM_TENANT));
//
//        //Determina el grupo (Group) y el recurso (resourceId)
//        String groupString = (String) jwtValues.get(JWT_CLAIM_GROUP);
//        Group group = Group.valueOf(groupString.toUpperCase());
//        String resourceId = TokenUtils.getJwtResourceIdClaim(jwt).orElse("");
//        UUID uuidResourceId = StringUtils.isNotBlank(resourceId) ? UUID.fromString(resourceId) : null;
//
//        //Crea el token de autenticación interno
//        //Retorna un JWTAuthenticationToken con:
//        //Usuario (username)
//        //Autoridades (GrantedAuthority) → scopes + role
//        //Grupo (Group)
//        //ResourceId (UUID)
//        //Esto se coloca en el contexto de Spring Security.
//        return new JWTAuthenticationToken(jwtValues.get(JWT_CLAIM_USERNAME), "", getAuthorities(jwtValues), group, uuidResourceId);
//    }
//
//
//    private void setTenant(String tenantId) throws ObjectNotFoundException {
//
//        //Busca el tenant en base al claim tenantId y lo guarda en un ThreadLocal (TenantContext),
//        // para que cualquier parte de la aplicación sepa en qué tenant está trabajando.
//        Tenant tenant = tenantService.findTenantById(UUID.fromString(tenantId));
//        TenantContext.setTenant(tenant);
//    }
//
//
//
//    @SuppressWarnings("unchecked")
//    private List<GrantedAuthority> getAuthorities(Map<String, Object> jwtValues) {
//
//        //Extrae del JWT la lista de permisos (scope) y el rol principal (role).
//        List<String> scopeList = (List<String>) jwtValues.get(JWT_CLAIM_SCOPE);
//
//        //Convierte cada uno en un GrantedAuthority, que es lo que Spring usa para autorizar endpoints (con @PreAuthorize, @Secured, etc.).
//        List<GrantedAuthority> grantedAuthorities = scopeList.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//
//        // agrega el rol principal del usuario (extraído del JWT) a la lista de permisos/autorizaciones (GrantedAuthority)
//        // que Spring Security usará para controlar el acceso.
//        grantedAuthorities.add(new SimpleGrantedAuthority((String) jwtValues.get(JWT_CLAIM_ROLE)));
//
//        return grantedAuthorities;
//    }

}




