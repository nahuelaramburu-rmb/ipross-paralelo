package com.capacidad.validationapi.module.base.model;

import com.capacidad.validationapi.config.multitenancy.TenantContext;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.TENANT_FILTER;

@MappedSuperclass
@FilterDef(name = TENANT_FILTER, parameters = {@ParamDef(name = "tenantId", type = "java.util.UUID")})
@Filter(name = TENANT_FILTER, condition = "tenant_id = :tenantId")
@Getter
@Setter
public abstract class BaseTenantEntity<I extends Serializable> extends BaseEntity<I> implements Serializable {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @PrePersist
    @Override
    protected void onPrePersist() {
        if (TenantContext.getTenant() != null) {
            this.setTenantId(TenantContext.getTenant());
            super.onPrePersist();
        }
    }

    @PreUpdate
    @Override
    protected void onPreUpdate() {
        super.onPreUpdate();
    }

}
