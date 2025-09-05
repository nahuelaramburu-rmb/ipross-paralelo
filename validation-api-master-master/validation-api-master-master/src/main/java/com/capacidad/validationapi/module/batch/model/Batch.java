package com.capacidad.validationapi.module.batch.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Status;
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
@Table(name = "batch")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "batch_seq", allocationSize = 1)
@Relation(collectionRelation = "batches")
public class Batch extends BaseTenantEntity<Long> {

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "batch_diagnosis",
            joinColumns = {@JoinColumn(name = "batch_id")},
            inverseJoinColumns = {@JoinColumn(name = "disease_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"batch_id", "disease_id"})
            }
    )
    private Set<ICD10Disease> diagnosis = new HashSet<>();

    @Audited(withModifiedFlag = true)
    @Column(length = 1000)
    private String description;

    @Audited(withModifiedFlag = true)
    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Audited(withModifiedFlag = true)
    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Audited(targetAuditMode = NOT_AUDITED, withModifiedFlag = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Audited(withModifiedFlag = true)
    @Column(name = "status_update_description")
    private String statusUpdateDescription;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<BatchItem> batchItems = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Override
    public void associateChildObjects() {
        this.batchItems.forEach(item -> item.setBatch(this));
    }

    public boolean isActive() {
        return this.getStatus().getId().equals(BATCH_ACTIVE.getId());
    }

    public boolean isPending() {
        return this.getStatus().getId().equals(BATCH_PENDING.getId());
    }

    public boolean nowIsAfterDateTo() {
        return LocalDate.now().isAfter(this.getDateTo());
    }

    public boolean nowIsEqualDateFrom() {
        return LocalDate.now().isEqual(this.getDateFrom());
    }

    public boolean isCancelled() {
        return this.getStatus().getId().equals(BATCH_CANCELLED.getId());
    }

    public boolean isExpired() {
        return this.getStatus().getId().equals(BATCH_EXPIRED.getId());
    }


}
