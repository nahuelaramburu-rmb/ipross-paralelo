package com.capacidad.validationapi.module.location.model;


import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.organization.model.Organization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "region",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "deleted", "deletion_token", "tenant_id"})
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "region_seq", allocationSize = 1)
@Relation(collectionRelation = "regions")
public class Region extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String name;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "region_city",
            joinColumns = {@JoinColumn(name = "region_id")},
            inverseJoinColumns = {@JoinColumn(name = "city_id")}
    )
    @BatchSize(size = 20)
    private Set<City> cities = new HashSet<>();

    @OneToMany(mappedBy = "region")
    @BatchSize(size = 20)
    private Set<AuditTray> auditTrays = new HashSet<>();

    @OneToMany(mappedBy = "region")
    @BatchSize(size = 20)
    private Set<ContractAdjustment> contractAdjustments = new HashSet<>();

    @OneToMany(mappedBy = "region")
    @BatchSize(size = 20)
    private Set<MedicalCoverage> medicalCoverages = new HashSet<>();

    @OneToMany(mappedBy = "region")
    @BatchSize(size = 20)
    private Set<Organization> organizations = new HashSet<>();

    public Set<Long> getCityIds() {
        return this.getCities()
                .stream()
                .map(City::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Region))
            return false;
        Region other = (Region) o;
        return other.getCityIds().containsAll(other.getCityIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cities);
    }

}
