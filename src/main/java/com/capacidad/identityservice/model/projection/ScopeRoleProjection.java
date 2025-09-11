package com.capacidad.identityservice.model.projection;

import com.capacidad.identityservice.model.Operation;

import java.util.List;

public interface ScopeRoleProjection {

    List<Operation> getOperations();

    ResourceProjection getResource();

    interface ResourceProjection {
        String getName();
    }

}
