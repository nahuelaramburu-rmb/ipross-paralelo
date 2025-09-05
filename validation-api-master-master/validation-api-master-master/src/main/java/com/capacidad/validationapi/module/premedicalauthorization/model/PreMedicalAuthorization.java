package com.capacidad.validationapi.module.premedicalauthorization.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "pre_medical_authorization", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code", "deleted", "deletion_token", "tenant_id"})
})
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "pre_medical_authorization_seq")
@Relation(collectionRelation = "preAuthorizations")
public class PreMedicalAuthorization extends BaseTenantEntity<Long> {

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petitioner_id")
    private Practitioner petitioner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", updatable = false)
    private Beneficiary beneficiary;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Audited(withModifiedFlag = true, targetAuditMode = NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Audited(withModifiedFlag = true)
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "preMedicalAuthorization", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<PreMedicalAuthorizationItem> preMedicalAuthorizationItems = new HashSet<>();

    @OneToMany(mappedBy = "preMedicalAuthorization")
    private Set<MedicalAuthorization> medicalAuthorizations = new HashSet<>();

    @Override
    public void associateChildObjects() {
        preMedicalAuthorizationItems.forEach(i -> i.setPreMedicalAuthorization(this));
    }

    public boolean isCancelled() {
        return this.getStatus().getId().equals(PRE_MEDICAL_AUTHORIZATION_CANCELLED.getId());
    }

    public boolean isActive() {
        return this.getStatus().getId().equals(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());
    }

    public boolean isExpired() {
        return this.getStatus().getId().equals(PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId()) ||
                this.getExpirationDate().isBefore(LocalDate.now());
    }

}
