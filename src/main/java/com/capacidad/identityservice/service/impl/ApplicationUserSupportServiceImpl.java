package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.base.BaseEntity;
import com.capacidad.identityservice.service.ApplicationUserSupportService;
import com.capacidad.identityservice.service.Authenticator;
import com.capacidad.identityservice.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization; //
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService; //
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;


import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 *
 * ctúa como un servicio centralizado para operaciones comunes sobre usuarios y seguridad:
 * <p>
 * Gestión de contraseñas (codificación y verificación).
 * <p>
 * Generación y validación de OTP (One-Time Password).
 * <p>
 * Preparación de plantillas dinámicas.
 * <p>
 * Interacción con tokens OAuth2 y clientes registrados para limpiar tokens de un usuario.
 * <p>
 * Acceso a propiedades de configuración de la aplicación.
 * <p>
 * En resumen: es un “helper” o soporte para funcionalidades de seguridad y usuario dentro de la aplicación.
 * <p>
 * <p>
 * Campo	                                            Tipo	                     Propósito
 * PasswordEncoder passwordEncoder	                    Spring Security	             Codificar y verificar contraseñas de usuarios.
 * Utils utils	                                        Custom	                     Métodos auxiliares, como obtener referencias a entidades de la DB.
 * Authenticator authenticator	                        Custom	                     Generación y validación de OTP.
 * ApplicationProperties applicationProperties	        Custom	                     Propiedades de configuración de la aplicación, como perfil activo o reintentos de verificación.
 * TemplateService templateService	                    Custom	                     Preparación de plantillas dinámicas con valores.
 * RegisteredClientRepository registeredClientRep..	    Spring Security OAuth2	     Gestiona clientes OAuth2 registrados.
 * OAuth2AuthorizationService authorizationService	    Spring Security OAuth2	     Almacena y maneja tokens de acceso y refresh tokens.
 *
 *
 */


// TODO : no es usado para nada este service en el contexto general del sistema ??
@Component
public class ApplicationUserSupportServiceImpl implements ApplicationUserSupportService {

    private final PasswordEncoder passwordEncoder;
    private final Utils utils;
    private final Authenticator authenticator;
    private final ApplicationProperties applicationProperties;
    private final TemplateService templateService;
   // private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final JdbcTemplate jdbcTemplate;

    public ApplicationUserSupportServiceImpl(PasswordEncoder passwordEncoder,
                                             Utils utils,
                                             Authenticator authenticator,
                                             ApplicationProperties applicationProperties,
                                             TemplateService templateService,
                                      //       RegisteredClientRepository registeredClientRepository,
                                             OAuth2AuthorizationService authorizationService,
                                             JdbcTemplate jdbcTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.utils = utils;
        this.authenticator = authenticator;
        this.applicationProperties = applicationProperties;
        this.templateService = templateService;
     //   this.registeredClientRepository = registeredClientRepository;
        this.authorizationService = authorizationService;
        this.jdbcTemplate = jdbcTemplate;
    }


    //Devuelve una referencia a una entidad en la base de datos usando su clase y primaryKey.
    //Es útil para obtener proxies de entidades sin cargarlas completamente
    public <T extends BaseEntity<I>, I extends Serializable> T getEntityReference(Class<T> clazz, Long primaryKey) {
        return utils.getEntityReference(clazz, primaryKey);
    }

    //Verifica si la contraseña sin codificar (rawPassword) coincide con la contraseña codificada (encodedPasswor)
    public boolean passwordMatches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // Codifica una contraseña para almacenarla de forma segura en la base de datos.
    public String encodePassword(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // Devuelve el número de intentos permitidos para verificación (OTP u otra acción sensible).
    public long getVerificationRetryProp() {
        return applicationProperties.getVerificationRetry();
    }

    // Devuelve el perfil activo de Spring (por ejemplo, dev, prod).
    @Override
    public String getActiveProfileProp() {
        return applicationProperties.getActiveProfile();
    }

    // Genera un código OTP(one time password)  usando un “salt” (semilla única).
    // todo , por ahora , solo en registro de user se genera el otp,
    //  ver manera para que pueda generarlo de forma independiente,  21/10/25
    @Override
    public Integer generateOtpCode(String salt) {
        return authenticator.generateOtp(salt);
    }

    // Valida que un OTP ingresado sea correcto para un determinado “salt”.
    // todo, modificar validateOtp , actualmente usa servicios de google,
    //  por ahora , validar otp contra el otp del user en db
    //  si el user no posee otp en db , buscar manera que lo pueda generar,  21/10/25
    @Override
    public boolean isOtpValid(int otp, String salt) {
        return authenticator.validateOtp(otp, salt);
    }

    // Llena un template con los valores proporcionados y devuelve el resultado.
    //Ejemplo: enviar un correo con un template donde se reemplazan variables como ${nombre} por los valores reales.
    @Override
    public String prepareTemplate(Map<String, String> values, String templateName) {
        return templateService.prepareTemplate(values, templateName);
    }



    //Limpia todos los tokens de acceso y refresh tokens asociados a un usuario específico (username).
    @Override
    public void clearTokensFromStore(String username) {

        //Verifica que el authorizationService realmente esté respaldado por JDBC.
        // Si no lo está (ej: en memoria), no tendría sentido ejecutar un DELETE
        if (!(authorizationService instanceof JdbcOAuth2AuthorizationService)) {
            throw new IllegalStateException(
                    "AuthorizationService no es de tipo JDBC, no puedo borrar tokens."
            );
        }

        // principal_name es la columna que guarda el username del usuario autenticado.
        // oauth2_authorization -> es la nueva tabla que debe crearse , para realizar la migracion a "Spring Authorization Server"
        String sql = "DELETE FROM oauth2_authorization WHERE principal_name = ?";

        // Borramos todos los registros asociados
        // rows -> cantidad de registros eliminados
        int rows = jdbcTemplate.update(sql, username);

        // TODO , cambiar a Logger
        System.out.println("Se eliminaron " + rows + " tokens para el usuario: " + username);
    }

}
