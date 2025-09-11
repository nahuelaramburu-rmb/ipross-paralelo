package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.model.Group;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;
import java.util.UUID;


/*
* Esta clase le da a Spring Security un token de autenticación específico para cuando el login se hace con un JWT.

* Así se puede:

Identificar el origen de la autenticación (ejemplo: si es con credenciales básicas → ClientAuthenticationToken, si es con JWT → JWTAuthenticationToken).
Acceder al resourceId fácilmente en cualquier capa sin tener que reparsear el token.
Separar responsabilidades: si en un futuro se necesita agregar validaciones específicas para JWT, se encapsulan aquí.
*
*
* */


@EqualsAndHashCode(callSuper = true)
@Getter
public class JWTAuthenticationToken extends BaseAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;


    //    Podría representar
//    El ID del recurso/aplicación al que está accediendo el usuario.
//    Un identificador único dentro del claim del token (ej: sub, aud o un claim custom de tu JWT).
//    El vínculo entre el usuario y un recurso protegido en tu sistema.
    private final UUID resourceId;

    public JWTAuthenticationToken(Object principal, Object credentials,
                                  Collection<? extends GrantedAuthority> authorities, Group group, UUID resourceId) {
        super(principal, credentials, authorities, group);
        this.resourceId = resourceId;
    }

}
