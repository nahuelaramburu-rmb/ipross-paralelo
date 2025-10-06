package com.capacidad.identityservice.model.projection;

import com.capacidad.identityservice.model.Operation;
import com.capacidad.identityservice.model.Resource;
import com.capacidad.identityservice.model.ScopeRole;

import java.util.List;


// define las operaciones sobre los recursos del sistema ,
// de acuerdo al rol del user,

public class ScopeRoleViewDTO {

    private final ScopeRole scopeRole;

    public ScopeRoleViewDTO(String resourceName, List<Operation> operations) {
        this.scopeRole = new ScopeRole();
        var resource = new Resource();
        resource.setName(resourceName);
        this.scopeRole.setResource(resource);
        this.scopeRole.setOperations(operations);
    }

    public ScopeRole getScopeRole() {
        return this.scopeRole;
    }

    public String getResourceName() {
        return scopeRole.getResource().getName();
    }

    public List<Operation> getOperations() {
        return scopeRole.getOperations();
    }

}
