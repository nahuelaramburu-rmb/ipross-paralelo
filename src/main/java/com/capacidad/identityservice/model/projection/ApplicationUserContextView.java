package com.capacidad.identityservice.model.projection;

import com.capacidad.identityservice.model.*;

import java.util.UUID;

public interface ApplicationUserContextView {

    Long getUserStateId();

    Long getId();

    Long getUserId();

    String getUserUsername();

    String getUserPassword();

    String getUserEmail();

    Long getRoleId();

    String getRoleName();

    UUID getTenantTenantId();

    String getTenantName();

    UUID getUserResourceId();

    UUID getUserSub();

    ChallengeType getUserChallengeType();

    PermissionStrategy getPermissionStrategy();

    Long getPermissionSuggestionId();

    default ApplicationUserContext buildContext() {
        var tenant = new Tenant();
        tenant.setTenantId(getTenantTenantId());
        tenant.setName(getTenantName());
        var role = new Role();
        role.setName(getRoleName());
        role.setId(getRoleId());
        var state = new State();
        state.setId(getUserStateId());

        var applicationUser = new ApplicationUser();
        applicationUser.setId(getUserId());
        applicationUser.setState(state);
        applicationUser.setUsername(getUserUsername());
        applicationUser.setPassword(getUserPassword());
        applicationUser.setEmail(getUserEmail());
        applicationUser.setResourceId(getUserResourceId());
        applicationUser.setSub(getUserSub());
        applicationUser.setChallengeType(getUserChallengeType());
        var context = new ApplicationUserContext();
        if (getPermissionSuggestionId() != null) {
            var permissionSuggestion = new PermissionSuggestion();
            permissionSuggestion.setId(getPermissionSuggestionId());
            context.setPermissionSuggestion(permissionSuggestion);
        }
        context.setPermissionStrategy(getPermissionStrategy());
        context.setTenant(tenant);
        context.setRole(role);
        context.setUser(applicationUser);
        context.setId(getId());

       // applicationUser.getContextSet().add(context);

        return context;
    }

}
