package com.capacidad.validationapi.module.properties.model;

import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.general.model.Period;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tenant_properties")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "properties_seq", allocationSize = 1)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Relation(collectionRelation = "properties")
public class Properties extends BaseEntity<Long> {

    @Column(name = "preauthorization_max_days", nullable = false)
    private Integer preAuthorizationMaxDays;

    @Column(name = "prescription_service")
    private String prescriptionService;

    @Enumerated(EnumType.STRING)
    @Column(name = "prescription_expiration_period")
    private Period prescriptionExpirationPeriod;

    @Column(name = "tenant_id", updatable = false)
    private UUID tenantId;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private Map<String, Object> mappings;

    @Column(name = "holder_beneficiary_min_age", nullable = false)
    private Integer holderBeneficiaryMinAge;

    @Column(name = "beneficiary_min_account_age", nullable = false)
    private Integer beneficiaryMinAccountAge;

    public Properties(Properties properties) {
        this.preAuthorizationMaxDays = properties.getPreAuthorizationMaxDays();
        this.prescriptionService = properties.getPrescriptionService();
        this.prescriptionExpirationPeriod = properties.getPrescriptionExpirationPeriod();
        this.tenantId = properties.getTenantId();
        this.mappings = properties.getMappings();
        this.holderBeneficiaryMinAge = properties.getHolderBeneficiaryMinAge();
        this.beneficiaryMinAccountAge = properties.getBeneficiaryMinAccountAge();
    }

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
