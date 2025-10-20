package com.capacidad.validationapi.module.organization.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.contract.model.OrganizationContract;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organization",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "address_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"region_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"resource_id", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "organization_seq", allocationSize = 1)
@Relation(collectionRelation = "organizations")
public class Organization extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", orphanRemoval = true)
    private Set<MedicalRegistration> medicalRegistrations = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_organization_id")
    private Organization relatedOrganization;

    @Column(name = "resource_id", nullable = false, updatable = false)
    private UUID resourceId = UUID.randomUUID();

    @OneToOne(mappedBy = "organization", fetch = FetchType.LAZY)
    private OrganizationContract contract;

}
