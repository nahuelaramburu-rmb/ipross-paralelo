package com.capacidad.identityservice.config.token;

import com.capacidad.identityservice.exception.TokenSigningException;
import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.model.projection.ScopeRoleViewDTO;
import com.capacidad.identityservice.repository.ScopeRoleRepository;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.PermissionGroupService;
import com.capacidad.identityservice.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COMA;


/*
* esta clase es un constructor de tokens JWT (Access Tokens).

    * Se encarga de:
    Tomar la autenticación (Authentication) y un cliente registrado (RegisteredClient).
    Generar un JWT con claims personalizados dependiendo si es:
        Un usuario autenticado con sus permisos y tenant.
        Un cliente "client-only" (sin usuario).
    Firmar ese JWT con el JwtEncoder
*
*  flujo de forma dinámica:

    Usuario autenticado: le mete datos del usuario, tenant, rol, permisos, email, etc.
    Client-only: crea un token más simple solo con el client_id.
    Finalmente, firma el token para que sea válido y verificable.
*
*
* */


@Slf4j
//@Component
public class TokenBuilderImpl {

    // Maneja validaciones de usuario en un contexto (estado operativo, reglas, etc.).
    private final ApplicationUserContextService userContextService;

    // Valida que el usuario esté asociado al tenant correcto.
    private final TenantService tenantService;

    // Obtiene permisos (grupos y operaciones) del usuario en base al tenant
    private final PermissionGroupService permissionGroupService;

    // Contiene configuraciones como el issuer del JWT y el perfil activo
    private final ApplicationProperties applicationProperties;

    // Se usa para firmar y codificar el JWT con las claims definidas
    private final JwtEncoder jwtEncoder;


    private final ScopeRoleRepository scopeRoleRepository;


    public TokenBuilderImpl(ApplicationUserContextService userContextService,
                            TenantService tenantService,
                            PermissionGroupService permissionGroupService,
                            ApplicationProperties applicationProperties,
                            JwtEncoder jwtEncoder, ScopeRoleRepository scopeRoleRepository) {
        this.userContextService = userContextService;
        this.tenantService = tenantService;
        this.permissionGroupService = permissionGroupService;
        this.applicationProperties = applicationProperties;
        this.jwtEncoder = jwtEncoder;
        this.scopeRoleRepository = scopeRoleRepository;

    }

    /**
     * Construye un JWT Access Token con claims personalizados
     * para usuarios y client-only.
     */
    public String buildAccessToken(
            Authentication authentication
            // RegisteredClient registeredClient
    ) {

        // Define el issuedAt y expiresAt según la configuración de RegisteredClient
        // todo , definir nueva duracion del token -> 1 hr
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600); // duración fija de 1 hora

//        Instant expiresAt = now.plusSeconds(
//                registeredClient.getTokenSettings().getAccessTokenTimeToLive().getSeconds()
//        );

        // Claims iniciales (comunes a todos los tokens)
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(applicationProperties.getJwtIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(authentication.getName());
        //   .claim("client_id", registeredClient.getClientId())
        //   .claim("scope", String.join(" ", registeredClient.getScopes()));  // todo , revisar scope


        // Si el Authentication es un usuario (CustomUserDetails)
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            // Obtiene el ApplicationUser
            ApplicationUser user = userDetails.getApplicationUser();

            // valida su tenant
//            ApplicationUserContext context = tenantService.validateTenant(user, registeredClient,
//                    userDetails.getTenantId());


            // obtengo el context atraves del user,
            Set<ApplicationUserContext> contextSet = user.getContextSet();

            // obtengo el primer contexto del user , en db , cada user solo tiene 1 contexto asociado
            Optional<ApplicationUserContext> contextOptional = contextSet.stream().findFirst();

            // el set que ira en el claim , donde guardara , operaciones sobre los distintos recursos, de acuerdo al rol del user
            Set<Map<String, Object>> scopeClaims = null;

            ApplicationUserContext context1 = null;

            // cada user tiene asociado 1 user context en db
            if (contextOptional.isPresent()) {

                context1 = contextOptional.get();

                Role role = context1.getRole();

                // obtener el scope de este rol, para setearlo en jwt
                // conjunto de operaciones sobre los distintos recursos del sistema,de un rol de user
                Set<ScopeRoleViewDTO> scopeRoleViewDTOS = scopeRoleRepository.findAllByRoleNameAndTenantIsNull(role.getName());

                // mapea el scopeRoleViewDTOS a estructuras simples para JSON en el claim
                scopeClaims = scopeRoleViewDTOS.stream()
                        .map(dto -> Map.of(
                                "resource", dto.getScopeRole().getResource().getName(),
                                "operations", dto.getScopeRole().getOperations()
                        ))
                        .collect(Collectors.toSet());
            }


            // Verifica su estado operativo (activo, bloqueado, etc.)
            //   userContextService.checkOperationalState(user, context, registeredClient);

            // Recupera grupos de permisos
//            Set<PermissionGroup> permissionGroups = permissionGroupService.findAllBasedOnContextAttributes(context);
//
//            // los convierte en un string de scopes extendidos (ejemplo: read:invoice,write:invoice
//            String newScope = permissionGroups.stream()
//                    .flatMap(p -> p.getResourceOperations().entrySet().stream()
//                            .flatMap(e -> e.getValue().stream()
//                                    .map(op -> op.toString().toLowerCase() + ":" + e.getKey())))
//                    .collect(Collectors.joining(COMA));


            //  Tenant tenant = context1.getTenant();

            // agrega claims personalizados
            assert scopeClaims != null;
            claimsBuilder
                    .claim("username", user.getUsername())
                    .claim("role", context1.getRole().getName().toLowerCase())

//                    .claim("tenant", Map.of(
//                            "id", tenant.getId(),
//                            "name", tenant.getName(),
//                            "role", context1.getRole().getName()
//                    ))

                    .claim("group", applicationProperties.getActiveProfile().toLowerCase())
                    .claim("scope", scopeClaims)
                    .claim("email", user.getEmail())
                    .claim("email_verified", user.getEmailVerified())
                    .claim("resource_id", user.getResourceId() != null ? user.getResourceId().toString() : "")
                    .claim("subrole", context1.getPermissionSuggestion() != null ? context1.getPermissionSuggestion().getId() : null);
            //   .claim("aud", registeredClient.getClientId());
        }

        // Caso client-only(sin usuario)
        else {
            claimsBuilder
                    .claim("username", "client")
                    .claim("role", "client")
                    .claim("group", applicationProperties.getActiveProfile().toLowerCase());
            //     .claim("tenant", Map.of("client_id", registeredClient.getClientId()))
            //   .claim("aud", registeredClient.getClientId());
        }

        // Firma del token
        // se construye claims
        JwtClaimsSet claims = claimsBuilder.build();

        try {
            // //Llama a jwtEncoder.encode(...) para firmar el token.
            return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        } catch (Exception e) {
            log.error("Error al firmar JWT: {}", e.getMessage());
            throw new TokenSigningException("tokenBuilder.signingError");
        }
    }
}
