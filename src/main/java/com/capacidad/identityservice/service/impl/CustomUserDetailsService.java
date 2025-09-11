package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Collections.emptyList;

/*
*
* UserDetailsService es una interfaz de Spring Security.
Spring Security la invoca cada vez que alguien intenta autenticarse (ej: login con username y password).
El metodo clave es loadUserByUsername(String username) → tiene que devolver un UserDetails (CustomUserDetail)
*
*
* Este servicio es el puente entre tu base de datos y Spring Security:

    Recibe el username (o email) cuando un usuario intenta loguearse.
    Busca en la base de datos el ApplicationUser  correspondiente (usando ApplicationUserContextService).
    Si lo encuentra → construye un CustomUserDetails con su username, password y un set de authorities.
    Si no lo encuentra → lanza UsernameNotFoundException.
    Devuelve el UserDetails para que Spring Security termine la autenticación.
*
*   todo , Ahora mismo esta implementación devuelve un usuario sin authorities (roles vacíos)
*
*
*   ApplicationUser = el modelo de usuario real en la aplicación (con username, password, email, etc.).
    ApplicationUserContext = La relación del usuario con un contexto (roles, tenant, permisos).
    CustomUserDetails = Adaptador que transforma un ApplicationUser en un UserDetails para que Spring Security lo pueda usar en la autenticación
*
* */


@Log4j2
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // se usa para buscar usuarios y sus contextos (tenant, permisos, etc.) en la base de datos.
    private final ApplicationUserContextService userContextService;

    @Autowired
    public CustomUserDetailsService(ApplicationUserContextService userContextService) {
        this.userContextService = userContextService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        // representa a los usuarios de este sistema
        ApplicationUser user;

        try {

            //ApplicationUserContext -> Sirve para saber en qué tenant o con qué roles el usuario puede loguearse

            // Esto devuelve un conjunto de contextos asociados al usuario
            Set<ApplicationUserContext> contextSet = userContextService.findAllContextsByUsernameOrEmail(username);

            // De todos los contextos (contextSet) agarra el primero (iterator().next()).
            //Luego obtiene el ApplicationUser relacionado.
            //si contextSet está vacío, esto puede tirar NoSuchElementException
            user = contextSet.iterator().next().getUser();

            //Si no encuentra nada en la base de datos, lanza UsernameNotFoundException.
            //Esto es lo que espera Spring Security para saber que el login falló porque el usuario no existe.
        } catch (ObjectNotFoundException e) {
            throw new UsernameNotFoundException(username);
        }


        //CustomUserDetails:
        //Envuelve un ApplicationUser para que Spring Security lo entienda como UserDetails.
        //  Ahí es donde Spring valida contraseña, cuenta expirada, authorities, etc.

        //asocia el ApplicationUser completo al CustomUserDetails
        CustomUserDetails userDetails = new CustomUserDetails(user.getUsername(), user.getPassword(), emptyList());

        //asocia el ApplicationUser completo al CustomUserDetails
        userDetails.setApplicationUser(user);

        //El objeto CustomUserDetails es entregado a Spring Security
        //Spring lo usará en:
        //Validación de la contraseña
        //Chequear si la cuenta está habilitada, bloqueada, expirada, etc.
        //Obtener authorities/roles del usuario autenticado
        return userDetails;
    }

}