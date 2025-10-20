package com.capacidad.validationapi.module.budget.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.general.model.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.PAYED;

@Entity
@Table(name = "budget")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "budget_seq")
@Relation(collectionRelation = "budgets")
public class Budget extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private BigDecimal total;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "budget", orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<BudgetItem> budgetItems = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Column
    private LocalDateTime closedAt;

    public boolean isPayed() {
        return this.getStatus().getId().equals(PAYED.getId());
    }

    @Override
    public void associateChildObjects() {
        this.budgetItems.forEach(budgetItem -> budgetItem.setBudget(this));
    }

}