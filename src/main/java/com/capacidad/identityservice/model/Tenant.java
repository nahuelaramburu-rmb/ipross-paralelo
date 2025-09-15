package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;


/*
* Tenant = una organización o "cliente empresarial" dentro del sistema multi-tenant.

    Sirve para aislar datos y configuraciones entre distintas organizaciones que usan el Identity Service.

    Se guarda en la DB (application_tenant) con su UUID.

    Cada RegisteredClient se asocia a un Tenant mediante el campo TENANT_ID en ClientSettings
    *
    *
    * Flujo típico:

        *Registro

        Creas un Tenant en la DB.

        Creas usuarios (ApplicationUser) asociados a ese tenant.

        Registras un RegisteredClient y guardas en clientSettings el TENANT_ID.

        Autenticación con Client Credentials

        Un cliente se autentica con clientId y clientSecret.

        Tu código (setTenantContext) lee el TENANT_ID del RegisteredClient.

        Carga el Tenant real desde la DB (tenantService.findTenantById).

        Lo coloca en TenantContext (probablemente un ThreadLocal) para usarlo durante la request.

        Autenticación de usuarios

        Si un usuario inicia sesión, también se valida que el ApplicationUser.tenant coincida con el Tenant actual cargado en el contexto.
*
* */

@Entity
@Table(name = "application_tenant")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_tenant_seq")
public class Tenant extends BaseEntity<Long> implements Serializable {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "uuid", nullable = false, unique = true)
    private UUID tenantId;

    public Tenant(String tenant1, String tenantOne) {
        super();
    }
}
