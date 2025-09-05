package com.capacidad.validationapi.module.beneficiary.model;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.batch.model.Batch;
import com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference;
import com.capacidad.validationapi.module.budget.model.BeneficiaryBudget;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.person.model.Person;
import com.capacidad.validationapi.module.person.model.RelationshipType;
import com.capacidad.validationapi.module.procedure.model.Procedure;
import com.capacidad.validationapi.module.tradeunion.model.TradeUnion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.HOLDER;
import static com.capacidad.validationapi.module.person.reference.RelationshipTypeReference.NEWBORN;

@Entity
@Table(name = "beneficiary",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"beneficiary_code", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"resource_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"id_number", "id_type_id", "deleted", "deletion_token", "tenant_id"}),
                @UniqueConstraint(columnNames = {"work_id_number", "deleted", "deletion_token", "tenant_id"}),
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "beneficiary_seq", allocationSize = 1)
@Relation(collectionRelation = "beneficiaries")
public class Beneficiary extends Person {

    @Audited(withModifiedFlag = true)
    @Column(name = "beneficiary_code", nullable = false)
    private String beneficiaryCode;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_beneficiary_id")
    private Beneficiary relatedBeneficiary;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_type_id", nullable = false)
    private RelationshipType relationshipType;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Audited(withModifiedFlag = true)
    @Column(name = "status_update_description")
    private String statusUpdateDescription;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_category_id")
    private BeneficiaryCategory beneficiaryCategory;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Expiration> expirations = new ArrayList<>();

    @Audited(withModifiedFlag = true)
    @Column(name = "family_id", nullable = false)
    private UUID familyId = UUID.randomUUID();


    @Column(name = "resource_id", nullable = false, updatable = false)
    private UUID resourceId = UUID.randomUUID();

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BeneficiaryInsurancePlan> beneficiaryInsurancePlans = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "beneficiary_disease",
            joinColumns = {@JoinColumn(name = "beneficiary_id")},
            inverseJoinColumns = {@JoinColumn(name = "disease_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"beneficiary_id", "disease_id"})
            }
    )
    private Set<ICD10Disease> diseases = new HashSet<>();

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Batch> batches = new HashSet<>();

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Procedure> procedures = new HashSet<>();

    @Column(name = "active_batch", nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean activeBatch = false;

    @OneToMany(mappedBy = "beneficiary")
    @BatchSize(size = 20)
    private Set<BeneficiaryBudget> budgets = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "beneficiary_trade_union",
            joinColumns = {@JoinColumn(name = "beneficiary_id")},
            inverseJoinColumns = {@JoinColumn(name = "trade_union_id")},
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"beneficiary_id", "trade_union_id"})
            }
    )
    private Set<TradeUnion> tradeUnions = new HashSet<>();

    @Override
    public void associateChildObjects() {
        this.getBeneficiaryInsurancePlans().forEach(bip -> bip.setBeneficiary(this));
    }

    public boolean isHolder() {
        return this.getRelationshipType().getId().equals(HOLDER.getId());
    }

    public boolean isNewborn() {
        return this.getRelationshipType().getId().equals(NEWBORN.getId());
    }

    public void makeHolder() {
        this.setFamilyId(UUID.randomUUID());
        this.setRelationshipType(HOLDER.getInstance());
        this.setRelatedBeneficiary(null);
    }

    public void relateToHolder(Beneficiary relatedTo) throws ObjectNotValidException {
        if (!relatedTo.isHolder())
            throw new ObjectNotValidException("beneficiary.notAHolder");
        this.setFamilyId(relatedTo.getFamilyId());
        this.setBeneficiaryCategory(relatedTo.getBeneficiaryCategory());
        this.setCompany(relatedTo.getCompany());
        this.setPaymentMethod(relatedTo.getPaymentMethod());
        this.setRelatedBeneficiary(relatedTo);
    }

    public boolean wasBornIn(int year) {
        return this.getBirthDate().getYear() != year;
    }

    public int getAge() {
        return Period.between(this.getBirthDate(), LocalDate.now()).getYears();
    }

    public boolean isPaymentMethodInAccordanceWithCompany() {
        long paymentMethodId = this.getPaymentMethod().getId();
        return paymentMethodId == PaymentMethodReference.VOLUNTARY.getId() ||
                (paymentMethodId == PaymentMethodReference.PAYCHECK.getId() && this.getCompany() != null);
    }

    public boolean hasActiveHealthCoverage() {
        return this.getStatus().getId().equals(StatusReference.BENEFICIARY_WITH_COVERAGE.getId());
    }

}
