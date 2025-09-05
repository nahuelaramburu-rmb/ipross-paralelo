package com.capacidad.validationapi.module.practitioner.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.organization.model.Organization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@Table(name = "practitioner_medical_registrations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"practitioner_id", "organization_id", "deleted", "deletion_token", "tenant_id"})
        }
)
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "practitioner_medical_registrations_seq", allocationSize = 1)
@Relation(collectionRelation = "medicalRegistrations")
public class MedicalRegistration extends BaseTenantEntity<Long> {

    @Column(name = "registration_code")
    private String registrationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practitioner_id", nullable = false)
    private Practitioner practitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

}
