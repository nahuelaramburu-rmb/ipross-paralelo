package com.capacidad.validationapi.module.audittray.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
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
@Table(name = "audit_tray",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"purpose", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"audit_tray_resource_id", "deleted", "deletion_token", "tenant_id"}),
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "audit_tray_seq", allocationSize = 1)
@Relation(collectionRelation = "auditTrays")
public class AuditTray extends BaseTenantEntity<Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "audit_tray_resource_id", nullable = false, updatable = false)
    private UUID resourceId = UUID.randomUUID();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "audit_tray_auditors",
            joinColumns = {@JoinColumn(name = "audit_tray_id")},
            inverseJoinColumns = {@JoinColumn(name = "auditor_id")}
    )
    @BatchSize(size = 20)
    private Set<Auditor> auditors = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "audit_tray_nomenclator",
            joinColumns = {@JoinColumn(name = "audit_tray_id")},
            inverseJoinColumns = {@JoinColumn(name = "nomenclator_id")}
    )
    @BatchSize(size = 20)
    private Set<Nomenclator> nomenclators = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column
    private String color;

}
