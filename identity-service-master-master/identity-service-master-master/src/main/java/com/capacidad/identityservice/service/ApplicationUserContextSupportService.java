package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.*;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ApplicationUserContextSupportService {

    Specification<ApplicationUserContext> buildUserSubAndTenantSpec(UUID sub);

    Specification<ApplicationUserContext> buildSpecFrom(Role role, UUID resourceId, String search) throws ObjectNotFoundException;

    Specification<ApplicationUserContext> buildSpecFrom(UUID sub);

    Map<String, Object> buildUserAndPermissionsSearchQueryHints(boolean readOnly, boolean booleanCacheable, boolean includePermGroups);

    <T> T buildProjection(Class<T> projectionClazz, Object source);

    void registerUserContextToNotificationService(ApplicationUserContext context) throws ObjectNotValidException;

    void unregisterUserContextFromNotificationService(ApplicationUserContext context) throws ObjectNotValidException;

    void sendConfirmationEmail(ApplicationUserContext userContext);

    void sendVerificationEmail(ApplicationUserContext userContext);

    void sendRestoreEmail(ApplicationUser user);

    void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext, PermissionSuggestion permissionSuggestion, List<PermissionGroup> permissionGroups) throws ObjectNotFoundException;

    void setPermissionsAndStrategyToContext(ApplicationUserContext currentContext) throws ObjectNotFoundException;

}
