package com.capacidad.validationapi.module.budget.model;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.company.model.Company;
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
public class BeneficiaryBudget extends Budget {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "beneficiary_id", updatable = false)
    private Beneficiary beneficiary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", updatable = false)
    private Company company;

}
