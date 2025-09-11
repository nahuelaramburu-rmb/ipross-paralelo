package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.model.Group;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


/*
*   spring Security representa la autenticación mediante objetos que implementan Authentication.
    Al extender de AbstractAuthenticationToken, esta clase pasa a ser un token de autenticación que Spring puede usar en el contexto de seguridad.
*
* Este tipo de clase se usa en procesos de autenticación personalizados en Spring Security, por ejemplo:

    Cuando implementas un AuthenticationProvider propio.
    Cuando quieres manejar información adicional junto con la autenticación estándar (en este caso, el Group).
    Para inyectar en el contexto de seguridad (SecurityContextHolder) un objeto que Spring Security pueda reconocer como autenticado.
*
*
*
* */


@EqualsAndHashCode(callSuper = true)
@Getter
public class BaseAuthenticationToken extends AbstractAuthenticationToken {

    //principal: representa al usuario autenticado o la identidad (puede ser el nombre de usuario, un objeto UserDetails, etc.).
    private final transient Object principal;

    //credentials: son las credenciales usadas para autenticarse (por ejemplo, una contraseña o un token JWT).
    private final transient Object credentials;

    //extiende el concepto de autenticación para asociar al usuario con un grupo específico. (DEV, PROD, TEST, UAT)
    private final Group group;


    BaseAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, Group group) {
        super(authorities);

        //Si la lista de authorities no está vacía, marca automáticamente el token como autenticado
        //solo si el usuario tiene permisos (authorities) , se setea como autenticado.
        //Esto evita que el token quede en estado "no autenticado" una vez que se construye correctamente.
        if (authorities != null && !authorities.isEmpty()) {
            super.setAuthenticated(true);
        }

        this.group = group;
        this.principal = principal;
        this.credentials = credentials;
    }

//    public Object getCredentials() {
//        return null;
//    }

}
