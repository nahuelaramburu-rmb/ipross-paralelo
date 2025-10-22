package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.AuthorityMapper;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.model.projection.ScopeRoleViewDTO;
import com.capacidad.identityservice.repository.ScopeRoleRepository;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/*
*
* UserDetailsService es una interfaz de Spring Security.
Spring Security la invoca cada vez que alguien intenta autenticarse (ej: login con username y password).
El metodo clave es loadUserByUsername(String username) → tiene que devolver un UserDetails (CustomUserDetail)
*
*
* Este servicio es el puente entre la base de datos y Spring Security:

    Recibe el username (o email) cuando un usuario intenta loguearse.
    Busca en la base de datos el ApplicationUser  correspondiente (usando ApplicationUserContextService).
    Si lo encuentra → construye un CustomUserDetails con su username, password y un set de authorities.
    Si no lo encuentra → lanza UsernameNotFoundException.
    Devuelve el UserDetails para que Spring Security termine la autenticación.
*
*
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
    private final ScopeRoleRepository scopeRoleRepository;

    @Autowired
    public CustomUserDetailsService(@Lazy ApplicationUserContextService userContextService, ScopeRoleRepository scopeRoleRepository) {
        this.userContextService = userContextService;
        this.scopeRoleRepository = scopeRoleRepository;
    }


    // todo , validar que obtenga la data necesaria para setear en claims del jwt !!! 2/10/25
    @Override
    public CustomUserDetails loadUserByUsername(String username) {

        // representa a los usuarios de este sistema
        ApplicationUser user;
        Tenant userTenant;
        Role userRole; //
        Set<GrantedAuthority> authorities = new HashSet<>();

        try {

            //ApplicationUserContext -> Sirve para saber en qué tenant o con qué roles el usuario puede loguearse

            // Esto devuelve un conjunto de contextos asociados al usuario
            Set<ApplicationUserContext> contextSet = userContextService.findAllContextsByUsernameOrEmail(username);

            if (contextSet == null || contextSet.isEmpty()) {
                log.error("No se encontró ApplicationUserContext para el usuario: {}", username);
                throw new UsernameNotFoundException("Usuario no tiene contextos válidos: " + username);
            }


            // De todos los contextos (contextSet) agarra el primero (iterator().next()).
            //Luego obtiene el ApplicationUser relacionado.
            //si contextSet está vacío, esto puede tirar NoSuchElementException
            user = contextSet.iterator().next().getUser();

            System.out.println("userr ; " + user.getUsername());

            //   userTenant = contextSet.iterator().next().getTenant();

            // obtengo el rol del user ( todo -> a cada rol se le asignara luego su authority)
            userRole = contextSet.iterator().next().getRole();


            //userRole.setName("ROLE_" + userRole.getName().toUpperCase());


            // obtengo las operaciones permitidas a el rol del user
            //   List<Operation> roleOperations = scopeRoleRepository.findAllOperationsByRoleName(userRole.getName());

//            authorities = roleOperations.stream()
//                    .map(op -> new SimpleGrantedAuthority(op.name())) // si Operation es enum
//                    .collect(Collectors.toSet());


            Set<ScopeRoleViewDTO> scopeRoleViewDTOS = scopeRoleRepository.findAllByRoleNameAndTenantIsNull(userRole.getName());

            // es necesario agregarle el prefijo ??
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getName().toUpperCase()));

            authorities = scopeRoleViewDTOS.stream()
                    .flatMap(dto -> dto.getOperations().stream()
                            .map(op -> new SimpleGrantedAuthority(
                                    dto.getResourceName().toLowerCase() + ":" + op.name().toLowerCase()
                            ))
                    )
                    .collect(Collectors.toSet());


            //Si no encuentra nada en la base de datos, lanza UsernameNotFoundException.
            //Esto es lo que espera Spring Security para saber que el login falló porque el usuario no existe.
        } catch (ObjectNotFoundException e) {
            throw new UsernameNotFoundException(username);
        }


        // el user contiene el tenant , roles , scope , del user,
        //  debo obtener de user : a que tenant pertenece el user , y en base a eso , ver si es user de la app mobile, ver sus roles , permisos , etc


        System.out.println("user email :" + user.getEmail());

        //solo creo un userDetails con username , password y su rol , authorities
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getEmail(), user.getPassword(), userRole, authorities
        );


        System.out.println("user encontrado " + userDetails.getUsername());


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