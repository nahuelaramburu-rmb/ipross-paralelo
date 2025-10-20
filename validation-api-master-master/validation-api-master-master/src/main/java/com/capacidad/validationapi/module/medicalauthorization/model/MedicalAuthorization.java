package com.capacidad.validationapi.module.medicalauthorization.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.rating.Rating;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "medical_authorization")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "medical_authorization_seq")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Relation(collectionRelation = "authorizations")
public class MedicalAuthorization extends BaseTenantEntity<Long> {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_id", nullable = false, updatable = false)
    private Practitioner practitioner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petitioner_id", nullable = false, updatable = false)
    private Practitioner petitioner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_center_id", nullable = false, updatable = false)
    private MedicalCenter medicalCenter;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", updatable = false)
    private Contract contract;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false, updatable = false)
    private Beneficiary beneficiary;

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id")
    private ICD10Disease disease;

    @Audited(withModifiedFlag = true)
    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    @Audited(withModifiedFlag = true)
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Audited(withModifiedFlag = true)
    @Column(name = "charge_value")
    private BigDecimal chargeTotal;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorization_type_id", nullable = false)
    private AuthorizationType authorizationType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorization_condition_id")
    private AuthorizationCondition authorizationCondition;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", updatable = false)
    private Company company;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "medicalAuthorization")
    @BatchSize(size = 100)
    private Set<Prescription> prescriptions = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "medical_authorization_coverage",
            joinColumns = {@JoinColumn(name = "medical_authorization_id")},
            inverseJoinColumns = {@JoinColumn(name = "medical_coverage_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"medical_authorization_id", "medical_coverage_id"})
            }
    )
    private Set<MedicalCoverage> medicalCoverages = new HashSet<>();


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false, updatable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_medical_authorization_id")
    private PreMedicalAuthorization preMedicalAuthorization;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "medicalAuthorization", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<MedicalAuthorizationItem> medicalAuthorizationItems = new HashSet<>();

    @Audited(withModifiedFlag = true, targetAuditMode = NOT_AUDITED)
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "medical_authorization_failure",
            joinColumns = {@JoinColumn(name = "medical_authorization_id")},
            inverseJoinColumns = {@JoinColumn(name = "failure_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"failure_id"})
            }
    )
    @BatchSize(size = 20)
    private Set<Failure> failures = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Audited(withModifiedFlag = true)
    @Column(columnDefinition = "boolean DEFAULT false", nullable = false)
    private Boolean audited = false;

    @Audited(withModifiedFlag = true)
    @Column(name = "refundable_items", columnDefinition = "boolean DEFAULT false", nullable = false)
    private Boolean refundableItems = false;

    @Audited(withModifiedFlag = true, targetAuditMode = NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "rating_id")
    private Rating rating;

    @Transient
    private byte[] digitalSignature;

    @Transient
    private Contract selectedContract;

    @Transient
    private PreMedicalAuthorization selectedPreMedicalAuthorization;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private Set<Message> messages = new HashSet<>();

    @Override
    public void associateChildObjects() {
        this.medicalAuthorizationItems.forEach(authorizationItem -> authorizationItem.setMedicalAuthorization(this));
        this.prescriptions.forEach(p -> p.setMedicalAuthorization(this));
    }

    public boolean isApproved() {
        return this.getStatus().getId().equals(VALIDATION_APPROVED.getId());
    }

    public boolean isPending() {
        return this.getStatus().getId().equals(VALIDATION_PENDING.getId());
    }

    public boolean isPartiallyApproved() {
        return this.getStatus().getId().equals(VALIDATION_PARTIALLY_APPROVED.getId());
    }

    public StatusReference resolveStatusReferenceBasedOnItems() {
        if (areAllItemsApproved())
            return VALIDATION_APPROVED;
        if (areAllItemsRejected())
            return VALIDATION_REJECTED;
        return isAnyItemPending() ? VALIDATION_PENDING
                : VALIDATION_PARTIALLY_APPROVED;
    }

    private boolean areAllItemsApproved() {
        return this.getMedicalAuthorizationItems().size() == this.getMedicalAuthorizationItems()
                .stream()
                .filter(MedicalAuthorizationItem::isApproved)
                .count();
    }

    private boolean areAllItemsRejected() {
        return this.getMedicalAuthorizationItems().size() == this.getMedicalAuthorizationItems()
                .stream()
                .filter(MedicalAuthorizationItem::isRejected)
                .count();
    }

    private boolean isAnyItemPending() {
        return this.getMedicalAuthorizationItems()
                .stream().anyMatch(MedicalAuthorizationItem::isPending);
    }

    public boolean containsPreMedicalAuthorization() {
        return this.getSelectedPreMedicalAuthorization() != null || this.getPreMedicalAuthorization() != null;
    }

    public boolean isBatchAppliedToAllItems() {
        return this.getMedicalAuthorizationItems().stream()
                .allMatch(i -> i.getBatchItem() != null);
    }

}
