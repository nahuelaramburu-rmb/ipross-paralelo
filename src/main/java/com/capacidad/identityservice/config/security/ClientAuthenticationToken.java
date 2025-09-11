package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.model.Group;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;


/*
* este token representa autenticación de un cliente (probablemente una aplicación externa o un servicio que consume este Identity Service).
*
*La existencia de esta clase tiene sentido en escenarios como:

Diferenciar autenticaciones:
Puede tener distintos tipos de autenticación (usuarios finales, clientes de API, administradores, etc.).
Usar clases diferentes  permite distinguir fácilmente el tipo de autenticación en filtros, AuthenticationProviders o servicios.

*
*
* */


@EqualsAndHashCode(callSuper = true)
@Getter
public class ClientAuthenticationToken extends BaseAuthenticationToken {

    //Esto asegura la compatibilidad al serializar/deserializar el objeto en entornos distribuidos
    // o cuando Spring Security guarda/restaura el contexto de seguridad.
    //Se apoya en la constante que Spring recomienda (SpringSecurityCoreVersion.SERIAL_VERSION_UID).
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    public ClientAuthenticationToken(Object principal, Object credentials,
                                     Collection<? extends GrantedAuthority> authorities, Group group) {

        super(principal, credentials, authorities, group);
    }

}