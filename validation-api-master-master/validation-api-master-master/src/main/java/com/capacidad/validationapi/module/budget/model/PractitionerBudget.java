package com.capacidad.validationapi.module.budget.model;

import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "budget_seq")
@Relation(collectionRelation = "budgets")
public class PractitionerBudget extends Budget {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "practitioner_id", updatable = false)
    private Practitioner practitioner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medical_center_id", updatable = false)
    private MedicalCenter medicalCenter;

}
