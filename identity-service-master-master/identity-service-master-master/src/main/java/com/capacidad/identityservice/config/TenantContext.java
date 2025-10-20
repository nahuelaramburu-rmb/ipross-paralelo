package com.capacidad.identityservice.config;

import com.capacidad.identityservice.model.Tenant;

public class TenantContext {

    private TenantContext() {

    }

    private static final ThreadLocal<Tenant> context = new ThreadLocal<>();

    public static Tenant getTenant() {
        return context.get();
    }

    public static void setTenant(Tenant tenant) {
        context.set(tenant);
    }

    public static void clearContext() {
        context.remove();
    }

}
