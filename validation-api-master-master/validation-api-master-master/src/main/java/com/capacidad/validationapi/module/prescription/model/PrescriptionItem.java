package com.capacidad.validationapi.module.prescription.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.medicine.model.Medicine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@Table(name = "prescription_item")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "prescription_item_seq", allocationSize = 1)
@Relation(collectionRelation = "prescriptionItems")
public class PrescriptionItem extends BaseTenantEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false, updatable = false)
    private Prescription prescription;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "daily_dosage")
    private String dailyDosage;

    @Column(name = "treatment_days")
    private Integer treatmentDays;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id")
    private ICD10Disease disease;

}
