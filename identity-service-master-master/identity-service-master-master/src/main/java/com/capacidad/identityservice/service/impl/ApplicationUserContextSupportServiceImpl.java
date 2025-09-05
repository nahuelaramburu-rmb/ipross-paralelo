package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.service.*;
import com.capacidad.identityservice.specification.SpecificationBuilder;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Subgraph;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.*;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.WHITESPACE;
import static com.capacidad.identityservice.misc.constant.ScopeConstants.READ;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.*;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;

@Service
public class ApplicationUserContextSupportServiceImpl implements ApplicationUserContextSupportService {

    private final SpelAwareProxyProjectionFactory projectionFactory;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final TemplateService templateService;
    private final PermissionGroupService permissionGroupService;
    private final RoleService roleService;
    private final SpecificationBuilder<ApplicationUserContext, Long> specificationBuilder;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ApplicationUserContextSupportServiceImpl(NotificationService notificationService,
                                                    EmailService emailService,
                                                    TemplateService templateService,
                                                    PermissionGroupService permissionGroupService,
                                                    @Qualifier("specBuilder") SpecificationBuilder<ApplicationUserContext, Long> specificationBuilder,
                                                    RoleService roleService) {
        this.notificationService = notificationService;
        this.projectionFactory = new SpelAwareProxyProjectionFactory();
        this.emailService = emailService;
        this.templateService = templateService;
        this.permissionGroupService = permissionGroupService;
        this.specificationBuilder = specificationBuilder;
        this.roleService = roleService;
    }

    @Override
    public Specification<ApplicationUserContext> buildUserSubAndTenantSpec(UUID sub) {
        return (root, query, builder) -> {
            var tenantJoin = root.join("tenant");
            var user = root.join("user");
            return builder.and(builder.equal(tenantJoin.get("tenantId"), Utils.getContextTenantId()),
                    builder.equal(user.get("sub"), sub));
        };
    }

    @Override
    public Specification<ApplicationUserContext> buildSpecFrom(Role role, UUID resourceId, String search) throws ObjectNotFoundException {
        var validatedRole = getValidatedRole(role);
        Specification<ApplicationUserContext> spec = (root, query, builder) -> {
            var tenantJoin = root.join("tenant");
            var roleJoin = root.join("role");
            var userJoin = root.join("user");
            Predicate pred = builder.and(builder.equal(tenantJoin.get("tenantId"), Utils.getContextTenantId()),
                    builder.equal(userJoin.get("group"), Utils.getAuthenticatedAuthorityGroup()));
            Optional<Predicate> roleSearch = buildRoleSearch(validatedRole, builder, roleJoin.get("name"));
            roleSearch.ifPresent(predicate -> pred.getExpressions().add(predicate));
            if (resourceId != null)
                pred.getExpressions().add(builder.equal(userJoin.get("resourceId"), getValidatedResourceId(resourceId)));
            return pred;
        };
        Optional<Specification<ApplicationUserContext>> searchSpec = specificationBuilder.parseAndBuild(search);
        if (searchSpec.isPresent())
            return searchSpec.get().and(spec);
        return spec;
    }

    @Override
    public Specification<ApplicationUserContext> buildSpecFrom(UUID sub) {
        return (root, query, builder) -> {
            var tenantJoin = root.join("tenant");
            var userJoin = root.join("user");
            return builder.and(builder.equal(tenantJoin.get("tenantId"), Utils.getContextTenantId()),
                    builder.equal(userJoin.get("group"), Utils.getAuthenticatedAuthorityGroup()),
                    builder.equal(userJoin.get("sub"), sub));
        };
    }

    private <T> Optional<Predicate> buildRoleSearch(Role role, CriteriaBuilder builder, Path<T> rolePath) {
        String authRole = Utils.getAuthenticatedAuthorityRoleName();
        if (role == null) {
            if (StringUtils.equals(authRole, ROLE_FUNDER_AUTHORITY.getAuthority()))
                return Optional.of(builder.notEqual(rolePath, ROLE_ADMIN_AUTHORITY.getAuthority()));
            if (!StringUtils.equals(authRole, ROLE_ADMIN_AUTHORITY.getAuthority())
                    && !StringUtils.equals(authRole, ROLE_FUNDER_AUTHORITY.getAuthority()))
                return Optional.of(builder.equal(rolePath, authRole));
        } else
            return Optional.of(builder.equal(rolePath, role.getName()));
        return Optional.empty();
    }

    private Role getValidatedRole(Role role) throws ObjectNotFoundException {
        if (role != null) {
            roleService.validateAuthorityRoleAccess(role, READ);
            return role;
        }
        return null;
    }

    private UUID getValidatedResourceId(UUID resourceId) {
        String roleNameToValidate = Utils.getAuthenticatedAuthorityRoleName();
        List<String> roleNames = Arrays.asList(MEDICAL_CENTER, ORGANIZATION, PRACTITIONER, BENEFICIARY);
        if (roleNames.contains(roleNameToValidate))
            return Utils.getAuthenticatedResourceId();
        return resourceId;
    }

    @Override
    public Map<String, Object> buildUserAndPermissionsSearchQueryHints(boolean readOnly, boolean booleanCacheable, boolean includePermGroups) {
        EntityGraph<ApplicationUserContext> entityGraph = entityManager.createEntityGraph(ApplicationUserContext.class);
        if (includePermGroups)
            entityGraph.addSubgraph("permissionGroups");
        entityGraph.addSubgraph("permissionSuggestion");
        entityGraph.addAttributeNodes("permissionStrategy");
        entityGraph.addSubgraph("role");
        Subgraph<ApplicationUser> userSubgraph = entityGraph.addSubgraph("user");
        userSubgraph.addSubgraph("profile");
        userSubgraph.addSubgraph("state");
        Map<String, Object> queryHints = new HashMap<>();
        queryHints.put(HINT_READONLY, readOnly);
        queryHints.put(HINT_CACHEABLE, booleanCacheable);
        queryHints.put("javax.persistence.fetchgraph", entityGraph);
        return queryHints;
    }

    @Override
    public <T> T buildProjection(Class<T> projectionClazz, Object source) {
        return projectionFactory.createProjection(projectionClazz, source);
    }

    @Override
    public void registerUserContextToNotificationService(ApplicationUserContext context) throws ObjectNotValidException {
        notificationService.registerUserContext(context);
    }

    @Override
    public void unregisterUserContextFromNotificationService(ApplicationUserContext context) throws ObjectNotValidException {
        notificationService.unregisterUserContext(context);
    }

    @Override
    public void sendConfirmationEmail(ApplicationUserContext userContext) {
        ApplicationUser user = userContext.getUser();
        String tenantName = userContext.getTenant().getName();
        String templateMimeText = templateService.prepareConfirmationEmail(user, tenantName);
        String title = templateService.getLocaleTitle("title.confirmationEmailTitle");
        emailService.sendMimeEmail(user.getEmail(), StringUtils.join(title, WHITESPACE, tenantName), templateMimeText, true);
    }

    @Override
    public void sendVerificationEmail(ApplicationUserContext userContext) {
        ApplicationUser user = userContext.getUser();
        String tenantName = userContext.getTenant().getName();
        String templateMimeText = templateService.prepareVerificationEmail(user, tenantName);
        String title = templateService.getLocaleTitle("title.verificationEmailTitle");
        emailService.sendMimeEmail(user.getEmail(), StringUtils.join(title, WHITESPACE, tenantName), templateMimeText, true);
    }

    @Override
    public void sendRestoreEmail(ApplicationUser user) {
        String templateMimeText = templateService.prepareRestorePasswordEmail(user);
        String title = templateService.getLocaleTitle("title.restorePasswordTitle");
        emailService.sendMimeEmail(user.getEmail(), title, templateMimeText, true);
    }

    @Override
    public void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext, PermissionSuggestion permissionSuggestion, List<PermissionGroup> permissionGroups) throws ObjectNotFoundException {
        permissionGroupService.setPermissionsAndStrategyToContext(currentContext, permissionSuggestion, permissionGroups);
    }

    @Override
    public void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext) throws ObjectNotFoundException {
        permissionGroupService.setPermissionsAndStrategyToContext(currentContext);
    }

}
