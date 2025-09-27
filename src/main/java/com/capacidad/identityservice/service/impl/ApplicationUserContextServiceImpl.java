package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.model.dto.UpdateApplicationUserContextDTO;
import com.capacidad.identityservice.model.projection.ApplicationUserContextProjection;
import com.capacidad.identityservice.model.projection.ApplicationUserContextView;
import com.capacidad.identityservice.repository.ApplicationUserContextRepository;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.identityservice.service.ApplicationUserContextSupportService;
import com.capacidad.identityservice.service.ApplicationUserService;
import com.capacidad.identityservice.service.RoleService;
import com.capacidad.identityservice.service.base.BaseServiceImpl;
import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ScopeConstants.*;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ROLE_CLIENT_AUTHORITY;


/*
*
*
*Esta clase es el núcleo del servicio de gestión de usuarios dentro de un tenant. Se encarga de:

    Crear contextos de usuario con roles, permisos y notificaciones.
    Validar que los roles y recursos cumplan las restricciones.
    Eliminar contextos o usuarios completos según corresponda.
    Gestionar flujos como recuperación de contraseña o verificación por email.
    Consultar usuarios y contextos con filtros avanzados.
    Asegurar siempre la validación de seguridad y autoridad del rol antes de cualquier operación.**
*
*
*El ApplicationUserContext representa la relación entre un usuario (ApplicationUser), un rol (Role), y el tenant en el que está operando.
* */


@Log4j2
@Service
public class ApplicationUserContextServiceImpl extends BaseServiceImpl<ApplicationUserContext, Long> implements ApplicationUserContextService {

    // accceso a db
    private final ApplicationUserContextRepository userContextRepository;

    // operaciones sobre usuarios
    private final ApplicationUserService userService;

    // gestión y validación de roles
    private final RoleService roleService;

    // lógica auxiliar: permisos, estrategias, notificaciones.
    private final ApplicationUserContextSupportService supportService;

    @Autowired
    public ApplicationUserContextServiceImpl(ApplicationUserContextRepository repository,
                                             ApplicationUserService userService,
                                             RoleService roleService,
                                             ApplicationUserContextSupportService supportService) {
        super(repository);
        this.userContextRepository = repository;
        this.userService = userService;
        this.roleService = roleService;
        this.supportService = supportService;
    }


    // create(ApplicationUserContext userContext)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApplicationUserContext create(ApplicationUserContext userContext) throws ObjectNotFoundException, ObjectNotValidException {

        //Loggea la creación.
        log.info("create - args: {}({})", userContext.getClass(), userContext);

        //Asigna el tenant actual al contexto.
        userContext.setTenant(TenantContext.getTenant());

        //Valida rol.
        validate(userContext);

        //Si la autenticación es cliente → crea usuario con signUp(), si no, con create().
        ApplicationUser user = isClientAuthority() ? userService.signUp(userContext.getUser())
                : userService.create(userContext.getUser());

        //Asocia el usuario al contexto.
        userContext.setUser(user);

        //Configura permisos y estrategia (supportService.setPermissionsAndStrategyToContext).
        supportService.setPermissionsAndStrategyToContext(userContext);

        // prepara relaciones hijo
        userContext.associateChildObjects();

        // Persiste el user context
        ApplicationUserContext contextResult = userContextRepository.saveAndFlush(userContext);

        //Registra el contexto en el servicio de notificaciones.
        supportService.registerUserContextToNotificationService(userContext);

        //Envía emails de verificación o cambio de contraseña según corresponda.
        sendEmail(userContext);

        //Devuelve el ApplicationUserContext creado.
        return contextResult;
    }


    @Override
    public void validate(ApplicationUserContext userContext) throws ObjectNotValidException, ObjectNotFoundException {

        //Si el rol viene definido:
        //  Busca el rol por nombre.
        // Si no → busca el rol del usuario autenticado.
        Role role = userContext.getRole() != null ? roleService.findRole(userContext.getRole().getName())
                : roleService.findRole(Utils.getAuthenticatedAuthorityRoleName());

        //Valida que el rol pueda ser usado para CREATE.
        roleService.validateAuthorityRoleAccess(role, CREATE);

        //Asigna el rol validado al contexto.
        userContext.setRole(role);

        //Limpia usuarios sin confirmar.
        userService.clearUnconfirmedUser(userContext.getUser());

        validateResource(userContext);
    }


    private void validateResource(ApplicationUserContext userContext) throws ObjectNotValidException {

        setAuthenticatedAuthorityResource(userContext);

        var role = userContext.getRole();

        UUID resourceId = userContext.getUser().getResourceId();

        //Si el rol requiere resourceId:
        if (Boolean.TRUE.equals(role.getResourceIdRequired())) {

            //Debe estar presente, si no → excepción.
            if (resourceId == null)
                throw new ObjectNotValidException("applicationUserContext.resourceIdRequirement");

            //Si el resourceId no es reusable y ya existe en otro usuario → excepción.
            if (!Boolean.TRUE.equals(role.getReusableResourceId()) && userService.existsByResourceId(resourceId))
                throw new ObjectAlreadyExistsException("applicationUserContext.resourceIdAlreadyExist", resourceId.toString());
        }
    }


    //Si en el token JWT hay un resourceId, lo asigna al usuario si aún no lo tiene.
    private void setAuthenticatedAuthorityResource(ApplicationUserContext userContext) {

        // Obtains the currently authenticated principal, or an authentication request token
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // valida si authentication es JWTAuthenticationToken
        if (authentication instanceof JWTAuthenticationToken) {

            // obtiene el user del contexto
            ApplicationUser user = userContext.getUser();

            // castea authentication a JWTAuthenticationToken
            var jwtAuthenticationToken = (JWTAuthenticationToken) authentication;

            // asigna al user el resourceid, si no lo tiene y el token contiene uno
            if (jwtAuthenticationToken.getResourceId() != null && user.getResourceId() == null)
                user.setResourceId(jwtAuthenticationToken.getResourceId());
        }
    }

    private boolean isClientAuthority() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.contains(ROLE_CLIENT_AUTHORITY);
    }

    private void sendEmail(ApplicationUserContext userContext) {
        ApplicationUser user = userContext.getUser();
        if (user.getChallengeType() == ChallengeType.EMAIL_VERIFICATION_REQUIRED)
            supportService.sendVerificationEmail(userContext);
        if (user.getChallengeType() == ChallengeType.FORCE_CHANGE_PASSWORD)
            supportService.sendConfirmationEmail(userContext);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(String username) throws ObjectNotFoundException, ObjectNotValidException {

        //Obtiene el tenantId actual.
        UUID tenantId = Utils.getContextTenantId();

        //Busca el ApplicationUserContext por username y tenantId.
        ApplicationUserContext userContext = userContextRepository
                .findByUserUsernameAndTenantTenantId
                        (username, tenantId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "applicationUserContext.notFound", username, tenantId != null ? tenantId.toString() : ""
                ));

        delete(userContext);
    }


    private void delete(ApplicationUserContext userContext) throws ObjectNotFoundException, ObjectNotValidException {

        //Valida que el rol permita DELETE.
        roleService.validateAuthorityRoleAccess(userContext.getRole(), DELETE);

        //Obtiene el usuario asociado.
        ApplicationUser user = userContext.getUser();

        //Verifica que el grupo del usuario coincida con el grupo del autenticado.
        var group = Utils.getAuthenticatedAuthorityGroup();

        if (user.getGroup() != group) {
            throw new InsufficientAuthenticationException("applicationUserContext.cannotDelete");
        }

        //Si el usuario tiene solo un contexto → elimina usuario completo.
        if (user.getContextSet().size() == 1) {
            userService.delete(user);
        } else {
            //Si tiene más de un contexto:
            //Quita este contexto del set.
            user.getContextSet().remove(userContext);

            //Actualiza el usuario.
            userService.update(user);
        }
        //Limpia los tokens del usuario.
        userService.clearUserTokens(user.getUsername());

        //Desregistra el contexto en el servicio de notificaciones.
        supportService.unregisterUserContextFromNotificationService(userContext);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAll(UUID resourceId) throws ObjectNotFoundException, ObjectNotValidException {
        // Obtiene el tenant actual.
        UUID tenantId = Utils.getContextTenantId();

        //Busca todos los contextos asociados a ese resourceId y tenant.
        Set<ApplicationUserContext> userContexts = userContextRepository.findAllByUserResourceIdAndTenantTenantId(resourceId, tenantId);

        // elimina todos los contextos
        for (ApplicationUserContext userContext : userContexts)
            delete(userContext);
    }



    @Override
    public void forgotPassword(String email) throws ObjectNotFoundException, ObjectNotValidException {

        // Restaura la contraseña del usuario
        ApplicationUser user = userService.restorePassword(email);

        //Si el estado del usuario ≠ CONFIRMED → lanza excepción.
        if (!user.getState().getId().equals(StateReference.CONFIRMED.getId()))
            throw new ObjectNotValidException("applicationUser.notConfirmed");

        //Envía correo de recuperación
        supportService.sendRestoreEmail(user);
    }



    @Override
    public void checkOperationalState(ApplicationUser user, ApplicationUserContext context, RegisteredClient registeredClient) {

        // Verifica que el usuario tenga estado válido
        userService.checkUserState(user);

        //Valida que el rol del contexto tenga acceso al scope del registeredClient
        roleService.validateClientRoleAccess(context.getRole(), registeredClient.getScopes());
    }


    @Override
    public Set<ApplicationUserContext> findAllContextsByUsernameOrEmail(String input) throws ObjectNotFoundException {

        //Busca todos los contextos por username o email.
        Set<ApplicationUserContextView> contextViews = userContextRepository.findAllByUserUsernameOrUserEmail(input, input);

        //Si no hay resultados → lanza excepción.
        if (contextViews.isEmpty())
            throw new ObjectNotFoundException("applicationUser.notFoundUsernameOrEmail", input);


        //Convierte cada ApplicationUserContextView en un ApplicationUserContext y lo retorna
        return contextViews.stream()
                .map(ApplicationUserContextView::buildContext)
                .collect(Collectors.toSet());
    }

    @Override
    public Page<ApplicationUserContextProjection.WithoutPermissionGroups> findUsers(UUID resourceId, String roleName, String search, Pageable pageable) throws ObjectNotFoundException {

        var pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, "createdAt");

        //Si se pasa un roleName → busca el rol.
        var role = roleName != null ? roleService.findRole(roleName) : null;

        //Construye una especificación (Specification) con filtros: rol, recurso, búsqueda.
        Specification<ApplicationUserContext> spec = supportService.buildSpecFrom(role, resourceId, search);

        //Ejecuta consulta en userContextRepository con proyección WithoutPermissionGroups.
        return userContextRepository.findAllProjectedBy(spec, ApplicationUserContextProjection.WithoutPermissionGroups.class, pageRequest, supportService.buildUserAndPermissionsSearchQueryHints(true, true, false));
    }



    @Override
    public ApplicationUserContextProjection.WithPermissionGroups findUser(UUID sub) throws ObjectNotFoundException {

        // Construye especificación para buscar por sub.
        Specification<ApplicationUserContext> spec = supportService.buildSpecFrom(sub);

        //Busca en repositorio con hints de permisos.
        var res = userContextRepository.find(spec, supportService.buildUserAndPermissionsSearchQueryHints(true, true, true))
                .orElseThrow(() -> new ObjectNotFoundException("applicationUserContext.subNotFound", sub.toString()));

        //Convierte el resultado en proyección WithPermissionGroups.
        return supportService.buildProjection(ApplicationUserContextProjection.WithPermissionGroups.class, res);
    }



    @Transactional
    @Override
    public ApplicationUserContextProjection update(UUID sub, UpdateApplicationUserContextDTO input) throws ObjectNotFoundException {

        //Busca el contexto por sub y tenant con hints.
        ApplicationUserContext context = userContextRepository.find(supportService.buildUserSubAndTenantSpec(sub),
                        supportService.buildUserAndPermissionsSearchQueryHints(false, false, true))
                .orElseThrow(() -> new ObjectNotFoundException("applicationUserContext.subNotFound", sub.toString()));

        //Valida que el rol permita UPDATE.
        roleService.validateAuthorityRoleAccess(context.getRole(), UPDATE);

        //Convierte el input DTO en un ApplicationUserContext temporal.
        ApplicationUserContext mappedContext = this.mapDtoToInput(input);

        //Actualiza permisos y estrategia con supportService.
        supportService.setPermissionsAndStrategyToContext(context, mappedContext.getPermissionSuggestion(), mappedContext.getPermissionGroups());

        //Actualiza datos del usuario .
        userService.update(context, input.getUser());

        //Guarda cambios en repositorio.
        var updatedContext = userContextRepository.save(context);

        //Devuelve la proyección del contexto actualizado.
        return supportService.buildProjection(ApplicationUserContextProjection.class, updatedContext);
    }

}
