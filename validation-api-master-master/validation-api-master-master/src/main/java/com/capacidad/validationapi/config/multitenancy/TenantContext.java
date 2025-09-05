package com.capacidad.validationapi.config.multitenancy;


import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getTenant() {
        return currentTenant.get();
    }

    public static void setTenant(UUID tenant) {
        currentTenant.set(tenant);
    }

    public static void clearTenant() {
        currentTenant.remove();
    }

}
