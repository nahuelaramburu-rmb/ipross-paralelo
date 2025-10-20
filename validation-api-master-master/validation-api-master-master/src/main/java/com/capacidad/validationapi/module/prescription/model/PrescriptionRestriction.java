package com.capacidad.validationapi.module.prescription.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "prescription_restriction")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "prescription_restriction_seq", allocationSize = 1)
@Audited
public class PrescriptionRestriction extends BaseTenantEntity<Long> {

    @Audited(targetAuditMode = NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_specialty_id", nullable = false)
    private MedicalSpecialty medicalSpecialty;

    /*TODO: Add Drug blocking list*/
}
