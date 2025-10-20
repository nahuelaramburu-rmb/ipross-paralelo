package com.capacidad.validationapi.module.prescription.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
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

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "prescription")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "prescription_seq", allocationSize = 1)
@Relation(collectionRelation = "prescriptions")
public class Prescription extends BaseTenantEntity<Long> {

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "exchange_id", unique = true)
    private Set<Long> exchangeId = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_center_id")
    private MedicalCenter medicalCenter;

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_authorization_id")
    private MedicalAuthorization medicalAuthorization;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<PrescriptionItem> prescriptionItems = new HashSet<>();

    @Column(nullable = false)
    private String observations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period expirationPeriod;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private String key;

    @Column(name = "transaction_key")
    private String transactionKey;

    @Audited(withModifiedFlag = true)
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "dtype", nullable = false)
    private String dType;

    @Column(name = "preauthorized", nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean preAuthorized = false;


    @Override
    public void associateChildObjects() {
        prescriptionItems.forEach(i -> i.setPrescription(this));
    }

}
