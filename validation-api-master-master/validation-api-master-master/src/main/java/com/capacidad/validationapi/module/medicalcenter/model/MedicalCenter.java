package com.capacidad.validationapi.module.medicalcenter.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.batch.model.BatchItem;
import com.capacidad.validationapi.module.contract.model.MedicalCenterContract;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "medical_center",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "address_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"resource_id", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "medical_center_seq", allocationSize = 1)
@Relation(collectionRelation = "medicalCenters")
public class MedicalCenter extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "medicalCenters")
    private Set<Practitioner> practitioners = new HashSet<>();

    @Column(name = "resource_id", nullable = false, updatable = false)
    private UUID resourceId = UUID.randomUUID();

    @OneToOne(mappedBy = "medicalCenter", fetch = FetchType.LAZY)
    private MedicalCenterContract contract;

    @ManyToMany(mappedBy = "medicalCenters")
    @BatchSize(size = 20)
    private Set<BatchItem> batchItems = new HashSet<>();

}
