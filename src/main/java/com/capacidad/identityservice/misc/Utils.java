package com.capacidad.identityservice.misc;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.BaseAuthenticationToken;
import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.exception.GlobalExceptionHandler;
import com.capacidad.identityservice.model.Group;
import com.capacidad.identityservice.model.Tenant;
import com.capacidad.identityservice.model.base.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

// paquete deprecado
//import org.springframework.security.oauth2.provider.ClientDetails;

// ahora se usa este
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COLON;
import static com.capacidad.identityservice.misc.constant.ControllerEndpoints.*;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_NAME;
import static com.capacidad.utils.Constants.ROLE_PREFIX;

@Slf4j
@Component
public class Utils {

    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Autowired
    public Utils(ObjectMapper objectMapper,
                 EntityManager entityManager) {
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    // busca un archivo dentro del classpath y devuelve su URL.
    public static URL getFileURL(String filename) {
        URL url = null;
        try {
            url = new ClassPathResource(filename).getURL();
        } catch (IOException e) {
            log.error("Could not read File '{}' to URL", filename);
        }
        return url;
    }

    // abre un recurso desde el classpath como InputStream
    private static InputStream readResource(String resourceName) throws IOException {
        return ResourceUtils.getURL(StringUtils.join("classpath:", resourceName)).openStream();
    }

    //devuelve un error HTTP en formato JSON
    public static void sendError(HttpServletResponse response, ResponseEntity responseEntity) throws IOException {

        String apiError = (String) responseEntity.getBody();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(responseEntity.getStatusCodeValue());

        if (StringUtils.isNotBlank(apiError))
            response.getWriter().write(apiError);
    }

    // permite identificar qué endpoints son de autenticación
    public static boolean isLoginOrLogoutEndpoint(HttpServletRequest request) {
        return StringUtils.equals(StringUtils.join(request.getContextPath(), ENDPOINT_LOGIN), request.getRequestURI())
                || StringUtils.equals(StringUtils.join(request.getContextPath(), ENDPOINT_LOGOUT), request.getRequestURI());
    }

    // permite identificar qué endpoints son de autenticación
    public static boolean isOAuthEndpoint(HttpServletRequest request) {
        return StringUtils.startsWithIgnoreCase(request.getRequestURI(), StringUtils.join(request.getContextPath(), ENDPOINT_OAUTH));
    }

    // limpia el sufijo "Exception" de un nombre de excepción.
    public static String processExceptionSimpleName(String exceptionSimpleName) {
        return StringUtils.remove(exceptionSimpleName, "Exception");
    }

    // ajusta el issuer del JWT dependiendo del perfil (prod, dev, etc.)
    public static String buildJwtIssuer(String iss, String activeProfile) {
        if (StringUtils.equals(activeProfile, "prod"))
            return iss;
        if (StringUtils.contains(iss, "https"))
            return StringUtils.replace(iss, "https://", StringUtils.join("https://", activeProfile, "-"));

        return StringUtils.replace(iss, "http://", StringUtils.join("http://", activeProfile, "-"));
    }


    // TODO , REVISAR registeredClient.getClientSettings().getSettings().get(key)
    // TODO , YA QUE DEBO ENCOTRAR DONDE SE CREA EL USER , Y POR ELLO , DONDE SE SETEAN SUS VALORES (KEY)
    //extrae metadatos adicionales de un cliente OAuth2 (ej. tenant, entorno "DEV/PROD/TEST")
    public static Optional<String> getClientAdditionalInformation(String key, RegisteredClient registeredClient) {

        if (registeredClient != null && registeredClient.getClientSettings() != null) {

            // metadata extra de clientes (ej: "DEV", "PROD", "TEST"  o  TENANT_ID , DE ACUERDO AL KEY QUE SE LE PASE!!).
            Object value = registeredClient.getClientSettings().getSettings().get(key);

            if (value != null) {
                // se retonar el group/tenant_id como string
                return Optional.of(value.toString());
            }
        }

        return Optional.empty();
    }

    //devuelve el Group del usuario autenticado
    public static Group getAuthenticatedAuthorityGroup() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof BaseAuthenticationToken ? ((BaseAuthenticationToken) authentication).getGroup()
                : null;
    }

    //obtiene el principal (usuario/identificador)
    public static String getAuthenticatedAuthorityPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof BaseAuthenticationToken ? (String) authentication.getPrincipal()
                : null;
    }

    //obtiene un UUID ligado al token JWT
    public static UUID getAuthenticatedResourceId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof JWTAuthenticationToken ? ((JWTAuthenticationToken) authentication).getResourceId()
                : null;
    }

    // devuelve el tenant actual desde TenantContext
    public static UUID getContextTenantId() {
        return TenantContext.getTenant() != null ? TenantContext.getTenant().getTenantId() : null;
    }

    //devuelve el rol del usuario autenticado (prefijado con ROLE_...
    public static String getAuthenticatedAuthorityRoleName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .filter(authority -> StringUtils.startsWith(authority.getAuthority(), ROLE_PREFIX))
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    // construye una respuesta nombreTenant:tenantId
    public static String buildTenantResponse(Tenant tenant) {
        if (tenant == null)
            return "";
        return String.join(COLON, tenant.getName(), tenant.getTenantId().toString());
    }

    // obtiene el tenantName y tenantId de un cliente OAuth2 (RegisteredClient) y los concatena
    public static String buildTenantResponse(RegisteredClient registeredClient) {
        String tenantName = getClientAdditionalInformation(TENANT_NAME, registeredClient).orElse("");
        String tenantId = getClientAdditionalInformation(TENANT_ID, registeredClient).orElse("");
        if (StringUtils.isNotBlank(tenantName) && StringUtils.isNotBlank(tenantId))
            return String.join(COLON, tenantName, tenantId);
        return "";
    }

    // genera un scope con el formato operacion:recurso
    public static String buildScope(String operation, String resource) {
        return String.join(COLON, operation.toLowerCase(), resource.toLowerCase());
    }

    // carga un recurso JSON y lo convierte a un JsonNode usando Jackson.
    public Optional<JsonNode> readResourceToJsonNode(String resourceName) {
        try (InputStream resourceStream = readResource(resourceName)) {
            return Optional.of(objectMapper.readTree(resourceStream));
        } catch (IOException e) {
            log.error("Resource: {} could not be read, error: {}", resourceName, e.getMessage());
        }
        return Optional.empty();
    }


    // obtiene una referencia a una entidad de JPA sin necesidad de cargarla completamente (proxy con EntityManager
    public <T extends BaseEntity<I>, I extends Serializable> T getEntityReference(Class<T> entityClass, Long primaryKey) {
        return entityManager.getReference(entityClass, primaryKey);
    }


    //busca dinámicamente un metodo handle<NombreDeLaExcepcion> en el GlobalExceptionHandler y lo invoca, para resolver la excepción específica
    public static Optional<ResponseEntity<Object>> resolveException(GlobalExceptionHandler handler, HttpServletRequest request, Exception ex) {
        String baseHandlerName = "handle";
        String handlerName = StringUtils.join(baseHandlerName, ex.getClass().getSimpleName());
        try {
            Method method = handler.getClass().getMethod(handlerName, HttpServletRequest.class, ex.getClass());
            return Optional.ofNullable((ResponseEntity<Object>) method.invoke(handler, request, ex));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Error resolving Original Exception: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public static boolean isNotCustomEndpoint(HttpServletRequest request) {
        return isLoginOrLogoutEndpoint(request) || isOAuthEndpoint(request);
    }

}
