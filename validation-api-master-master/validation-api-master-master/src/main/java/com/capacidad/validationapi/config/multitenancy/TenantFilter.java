package com.capacidad.validationapi.config.multitenancy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated Method should not be filtered by Tenant Discriminator Column
 */

@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface TenantFilter {
    boolean active() default true;
}
