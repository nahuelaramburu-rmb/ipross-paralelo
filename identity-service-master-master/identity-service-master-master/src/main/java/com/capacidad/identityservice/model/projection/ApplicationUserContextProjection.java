package com.capacidad.identityservice.model.projection;

import com.capacidad.identityservice.model.PermissionStrategy;

import java.util.List;

public interface ApplicationUserContextProjection {

    ApplicationUserProjection getUser();

    List<PermissionGroupProjection> getPermissionGroups();

    IdAndNameProjection getPermissionSuggestion();

    PermissionStrategy getPermissionStrategy();

    interface WithoutPermissionGroups {
        ApplicationUserProjection getUser();

        IdAndNameProjection getRole();

        IdAndNameProjection getPermissionSuggestion();

        PermissionStrategy getPermissionStrategy();
    }

    interface WithPermissionGroups {
        ApplicationUserProjection getUser();

        IdAndNameProjection getRole();

        List<PermissionGroupProjection> getPermissionGroups();

        IdAndNameProjection getPermissionSuggestion();

        PermissionStrategy getPermissionStrategy();
    }

}
