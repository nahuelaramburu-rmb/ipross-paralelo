package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.Utils;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.PermissionGroup;
import com.capacidad.identityservice.model.PermissionStrategy;
import com.capacidad.identityservice.model.PermissionSuggestion;
import com.capacidad.identityservice.model.projection.PermissionGroupProjection;
import com.capacidad.identityservice.model.projection.PermissionSuggestionProjection;
import com.capacidad.identityservice.repository.PermissionGroupRepository;
import com.capacidad.identityservice.repository.PermissionSuggestionRepository;
import com.capacidad.identityservice.service.PermissionGroupService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.SecurityConstants.ROLE_ADMIN_AUTHORITY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_READONLY;

@Service
public class PermissionGroupServiceImpl implements PermissionGroupService {

    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionSuggestionRepository permissionSuggestionRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public PermissionGroupServiceImpl(PermissionGroupRepository permissionGroupRepository,
                                      PermissionSuggestionRepository permissionSuggestionRepository) {
        this.permissionGroupRepository = permissionGroupRepository;
        this.permissionSuggestionRepository = permissionSuggestionRepository;
    }

    @Override
    public List<PermissionGroupProjection> getAllPermissionGroups() {
        return permissionGroupRepository.findAllProjectedBy();
    }

    @Override
    public List<PermissionGroupProjection> findAllPermissionGroupsByRole(String role) {
        return permissionGroupRepository.findAllProjectedByRolesName(role.toUpperCase());
    }

    @Override
    public List<PermissionSuggestionProjection> findAllPermissionSuggestionsByRole(String role) {
        return permissionSuggestionRepository.findAllProjectedByRoleName(role.toUpperCase());
    }

    @Override
    public Set<PermissionGroup> findAllBasedOnContextAttributes(ApplicationUserContext context) {
        var permissionStrategy = context.getPermissionStrategy();
        Map<String, Object> hints = buildUserAndPermissionsSearchQueryHints();
        if (permissionStrategy.equals(PermissionStrategy.DEFAULT_ROLE))
            return new HashSet<>(permissionGroupRepository.findAll(buildRolesIdSpec(context.getRole().getId()), hints));
        if (permissionStrategy.equals(PermissionStrategy.PERMISSION_SUGGESTION))
            return new HashSet<>(permissionGroupRepository.findAll(buildPermissionSuggestionIdSpec(context.getPermissionSuggestion().getId()), hints));
        return new HashSet<>(permissionGroupRepository.findAll(buildUserContextIdSpec(context.getId()), hints));
    }

    @Override
    public void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext) throws ObjectNotFoundException {
        setPermissionsAndStrategyToContext(currentContext, currentContext.getPermissionSuggestion(), currentContext.getPermissionGroups());
    }

    @Override
    public void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext, PermissionSuggestion permissionSuggestion, List<PermissionGroup> permissionGroups) throws ObjectNotFoundException {
        if (Utils.getAuthenticatedAuthorityRoleName().equals(ROLE_ADMIN_AUTHORITY.getAuthority())) {
            validateSuggestionNotGroups(currentContext, permissionSuggestion, permissionGroups);
            validateNotSuggestionButGroups(currentContext, permissionSuggestion, permissionGroups);
            validateNotSuggestionNotGroupsOrBoth(currentContext, permissionSuggestion, permissionGroups);
        }
        if (currentContext.getPermissionStrategy() == null) {
            currentContext.setPermissionStrategy(PermissionStrategy.DEFAULT_ROLE);
        }
    }

    private void validateSuggestionNotGroups(ApplicationUserContext context, PermissionSuggestion permissionSuggestion, List<PermissionGroup> permissionGroups) throws ObjectNotFoundException {
        if (permissionSuggestion != null && !containsPermissionGroups(permissionGroups)) {
            var initializedPermissionSuggestion = findPermissionSuggestionById(permissionSuggestion.getId());
            context.setPermissionSuggestion(initializedPermissionSuggestion);
            context.setPermissionGroups(new ArrayList<>());
            context.setPermissionStrategy(PermissionStrategy.PERMISSION_SUGGESTION);
        }
    }

    private boolean containsPermissionGroups(List<PermissionGroup> permissionGroups) {
        return permissionGroups != null && !permissionGroups.isEmpty();
    }

    private void validateNotSuggestionButGroups(ApplicationUserContext context, PermissionSuggestion permissionSuggestion, List<PermissionGroup> permissionGroups) {
        if (permissionSuggestion == null && containsPermissionGroups(permissionGroups)) {
            context.setPermissionSuggestion(null);
            Set<Long> permIds = permissionGroups.stream()
                    .map(PermissionGroup::getId)
                    .collect(Collectors.toSet());
            List<PermissionGroup> initializedPermissionGroups = permissionGroupRepository.findAllByIdIn(permIds);
            context.setPermissionGroups(initializedPermissionGroups);
            context.setPermissionStrategy(PermissionStrategy.PERMISSION_GROUPS);
        }
    }

    private void validateNotSuggestionNotGroupsOrBoth(ApplicationUserContext context, PermissionSuggestion permissionSuggestion, List<PermissionGroup> permissionGroups) {
        if ((permissionSuggestion == null && !containsPermissionGroups(permissionGroups))
                || (permissionSuggestion != null && containsPermissionGroups(permissionGroups))) {
            context.setPermissionSuggestion(null);
            context.setPermissionGroups(new ArrayList<>());
            context.setPermissionStrategy(PermissionStrategy.DEFAULT_ROLE);
        }
    }

    private PermissionSuggestion findPermissionSuggestionById(long permissionSuggestionId) throws ObjectNotFoundException {
        return permissionSuggestionRepository.findById(permissionSuggestionId)
                .orElseThrow(() -> new ObjectNotFoundException(""));
    }

    private Specification<PermissionGroup> buildRolesIdSpec(long roleId) {
        return (root, query, builder) -> {
            var roles = root.join("roles");
            return builder.equal(roles.get("id"), roleId);
        };
    }

    private Specification<PermissionGroup> buildUserContextIdSpec(long contextId) {
        return (root, query, builder) -> {
            var contexts = root.join("userContexts");
            return builder.equal(contexts.get("id"), contextId);
        };
    }

    private Specification<PermissionGroup> buildPermissionSuggestionIdSpec(long permissionSuggestionId) {
        return (root, query, builder) -> {
            var permissionSuggestions = root.join("permissionSuggestions");
            return builder.equal(permissionSuggestions.get("id"), permissionSuggestionId);
        };
    }

    private Map<String, Object> buildUserAndPermissionsSearchQueryHints() {
        EntityGraph<PermissionGroup> entityGraph = entityManager.createEntityGraph(PermissionGroup.class);
        entityGraph.addAttributeNodes("resourceOperations");
        Map<String, Object> queryHints = new HashMap<>();
        queryHints.put(HINT_READONLY, true);
        queryHints.put(HINT_CACHEABLE, true);
        queryHints.put("javax.persistence.fetchgraph", entityGraph);
        return queryHints;
    }

}
