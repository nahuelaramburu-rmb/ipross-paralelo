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
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ScopeConstants.*;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.ROLE_CLIENT_AUTHORITY;

@Log4j2
@Service
public class ApplicationUserContextServiceImpl extends BaseServiceImpl<ApplicationUserContext, Long> implements ApplicationUserContextService {

    private final ApplicationUserContextRepository userContextRepository;
    private final ApplicationUserService userService;
    private final RoleService roleService;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApplicationUserContext create(ApplicationUserContext userContext) throws ObjectNotFoundException, ObjectNotValidException {
        log.info("create - args: {}({})", userContext.getClass(), userContext);
        userContext.setTenant(TenantContext.getTenant());
        validate(userContext);
        ApplicationUser user = isClientAuthority() ? userService.signUp(userContext.getUser())
                : userService.create(userContext.getUser());
        userContext.setUser(user);
        supportService.setPermissionsAndStrategyToContext(userContext);
        userContext.associateChildObjects();
        ApplicationUserContext contextResult = userContextRepository.saveAndFlush(userContext);
        supportService.registerUserContextToNotificationService(userContext);
        sendEmail(userContext);
        return contextResult;
    }

    @Override
    public void validate(ApplicationUserContext userContext) throws ObjectNotValidException, ObjectNotFoundException {
        Role role = userContext.getRole() != null ? roleService.findRole(userContext.getRole().getName())
                : roleService.findRole(Utils.getAuthenticatedAuthorityRoleName());
        roleService.validateAuthorityRoleAccess(role, CREATE);
        userContext.setRole(role);
        userService.clearUnconfirmedUser(userContext.getUser());
        validateResource(userContext);
    }

    private void validateResource(ApplicationUserContext userContext) throws ObjectNotValidException {
        setAuthenticatedAuthorityResource(userContext);
        var role = userContext.getRole();
        UUID resourceId = userContext.getUser().getResourceId();
        if (Boolean.TRUE.equals(role.getResourceIdRequired())) {
            if (resourceId == null)
                throw new ObjectNotValidException("applicationUserContext.resourceIdRequirement");
            if (!Boolean.TRUE.equals(role.getReusableResourceId()) && userService.existsByResourceId(resourceId))
                throw new ObjectAlreadyExistsException("applicationUserContext.resourceIdAlreadyExist", resourceId.toString());
        }
    }

    private void setAuthenticatedAuthorityResource(ApplicationUserContext userContext) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JWTAuthenticationToken) {
            ApplicationUser user = userContext.getUser();
            var jwtAuthenticationToken = (JWTAuthenticationToken) authentication;
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
        UUID tenantId = Utils.getContextTenantId();
        ApplicationUserContext userContext = userContextRepository
                .findByUserUsernameAndTenantTenantId
                        (username, tenantId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "applicationUserContext.notFound", username, tenantId != null ? tenantId.toString() : ""
                ));
        delete(userContext);
    }

    private void delete(ApplicationUserContext userContext) throws ObjectNotFoundException, ObjectNotValidException {
        roleService.validateAuthorityRoleAccess(userContext.getRole(), DELETE);
        ApplicationUser user = userContext.getUser();
        var group = Utils.getAuthenticatedAuthorityGroup();
        if (user.getGroup() != group)
            throw new InsufficientAuthenticationException("applicationUserContext.cannotDelete");
        if (user.getContextSet().size() == 1)
            userService.delete(user);
        else {
            user.getContextSet().remove(userContext);
            userService.update(user);
        }
        userService.clearUserTokens(user.getUsername());
        supportService.unregisterUserContextFromNotificationService(userContext);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAll(UUID resourceId) throws ObjectNotFoundException, ObjectNotValidException {
        UUID tenantId = Utils.getContextTenantId();
        Set<ApplicationUserContext> userContexts = userContextRepository.findAllByUserResourceIdAndTenantTenantId(resourceId, tenantId);
        for (ApplicationUserContext userContext : userContexts)
            delete(userContext);
    }

    @Override
    public void forgotPassword(String email) throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUser user = userService.restorePassword(email);
        if (!user.getState().getId().equals(StateReference.CONFIRMED.getId()))
            throw new ObjectNotValidException("applicationUser.notConfirmed");
        supportService.sendRestoreEmail(user);
    }

    @Override
    public void checkOperationalState(ApplicationUser user, ApplicationUserContext context, ClientDetails clientDetails) {
        userService.checkUserState(user);
        roleService.validateClientRoleAccess(context.getRole(), clientDetails.getScope());
    }

    @Override
    public Set<ApplicationUserContext> findAllContextsByUsernameOrEmail(String input) throws ObjectNotFoundException {
        Set<ApplicationUserContextView> contextViews = userContextRepository.findAllByUserUsernameOrUserEmail(input, input);
        if (contextViews.isEmpty())
            throw new ObjectNotFoundException("applicationUser.notFoundUsernameOrEmail", input);
        return contextViews.stream()
                .map(ApplicationUserContextView::buildContext)
                .collect(Collectors.toSet());
    }

    @Override
    public Page<ApplicationUserContextProjection.WithoutPermissionGroups> findUsers(UUID resourceId, String roleName, String search, Pageable pageable) throws ObjectNotFoundException {
        var pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, "createdAt");
        var role = roleName != null ? roleService.findRole(roleName) : null;
        Specification<ApplicationUserContext> spec = supportService.buildSpecFrom(role, resourceId, search);
        return userContextRepository.findAllProjectedBy(spec, ApplicationUserContextProjection.WithoutPermissionGroups.class, pageRequest, supportService.buildUserAndPermissionsSearchQueryHints(true, true, false));
    }

    @Override
    public ApplicationUserContextProjection.WithPermissionGroups findUser(UUID sub) throws ObjectNotFoundException {
        Specification<ApplicationUserContext> spec = supportService.buildSpecFrom(sub);
        var res = userContextRepository.find(spec, supportService.buildUserAndPermissionsSearchQueryHints(true, true, true))
                .orElseThrow(() -> new ObjectNotFoundException("applicationUserContext.subNotFound", sub.toString()));
        return supportService.buildProjection(ApplicationUserContextProjection.WithPermissionGroups.class, res);
    }

    @Transactional
    @Override
    public ApplicationUserContextProjection update(UUID sub, UpdateApplicationUserContextDTO input) throws ObjectNotFoundException {

        ApplicationUserContext context = userContextRepository.find(supportService.buildUserSubAndTenantSpec(sub),
                supportService.buildUserAndPermissionsSearchQueryHints(false, false, true))
                .orElseThrow(() -> new ObjectNotFoundException("applicationUserContext.subNotFound", sub.toString()));

        roleService.validateAuthorityRoleAccess(context.getRole(), UPDATE);

        ApplicationUserContext mappedContext = this.mapDtoToInput(input);

        supportService.setPermissionsAndStrategyToContext(context, mappedContext.getPermissionSuggestion(), mappedContext.getPermissionGroups());

        userService.update(context, input.getUser());

        var updatedContext = userContextRepository.save(context);

        return supportService.buildProjection(ApplicationUserContextProjection.class, updatedContext);
    }

}
