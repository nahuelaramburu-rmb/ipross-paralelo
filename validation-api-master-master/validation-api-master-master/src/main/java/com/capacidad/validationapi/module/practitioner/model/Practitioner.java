package com.capacidad.validationapi.module.practitioner.model;

import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.PractitionerContract;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.person.model.Person;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.rating.Rating;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "practitioner",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"practitioner_code", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"resource_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"id_number", "id_type_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"work_id_number", "deleted", "deletion_token", "tenant_id"}),
        }
)
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "practitioner_seq", allocationSize = 1)
@Relation(collectionRelation = "practitioners")
public class Practitioner extends Person {

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_category_id")
    private PractitionerCategory practitionerCategory;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Audited(withModifiedFlag = true)
    @Column(name = "status_update_description")
    private String statusUpdateDescription;

    @Audited(withModifiedFlag = true)
    @Column(name = "practitioner_code", nullable = false)
    private String practitionerCode;

    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "practitioner",
            orphanRemoval = true)
    private Set<MedicalRegistration> medicalRegistrations = new HashSet<>();

    @Audited(withModifiedFlag = true)
    @Column(name = "transaction_key")
    private String transactionKey;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "rating_id")
    private Rating rating;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "practitioner_medical_specialties",
            joinColumns = {@JoinColumn(name = "practitioner_id")},
            inverseJoinColumns = {@JoinColumn(name = "medical_specialty_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"practitioner_id", "medical_specialty_id"})
            }
    )
    private Set<MedicalSpecialty> medicalSpecialties = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "practitioner_medical_centers",
            joinColumns = {@JoinColumn(name = "practitioner_id")},
            inverseJoinColumns = {@JoinColumn(name = "medical_center_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"practitioner_id", "medical_center_id"})
            }
    )
    private Set<MedicalCenter> medicalCenters = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "practitioner_contracts",
            joinColumns = {@JoinColumn(name = "practitioner_id")},
            inverseJoinColumns = {@JoinColumn(name = "contract_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"practitioner_id", "contract_id"})
            }
    )
    private Set<Contract> contracts = new HashSet<>();

    @ManyToMany(mappedBy = "practitioners")
    @BatchSize(size = 20)
    private Set<BatchItem> batchItems = new HashSet<>();


    @Column(name = "resource_id", nullable = false, updatable = false)
    private UUID resourceId = UUID.randomUUID();

    @OneToOne(mappedBy = "practitioner", fetch = FetchType.LAZY)
    private PractitionerContract contract;

    @OneToMany(mappedBy = "practitioner")
    @BatchSize(size = 20)
    private Set<PractitionerBudget> budgets = new HashSet<>();

    @OneToMany(mappedBy = "petitioner")
    @BatchSize(size = 20)
    private Set<PreMedicalAuthorization> preMedicalAuthorizations = new HashSet<>();

    @Override
    public void associateChildObjects() {
        this.medicalRegistrations.forEach(mp -> mp.setPractitioner(this));
    }

}
