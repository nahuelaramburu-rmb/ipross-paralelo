package com.capacidad.validationapi.module.budget.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.budget.model.BudgetItem;
import com.capacidad.validationapi.module.budget.model.PractitionerBudget;
import com.capacidad.validationapi.module.budget.repository.PractitionerBudgetRepository;
import com.capacidad.validationapi.module.budget.service.BudgetItemService;
import com.capacidad.validationapi.module.budget.service.PractitionerBudgetService;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.render.service.RenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference.VOLUNTARY;
import static com.capacidad.validationapi.module.general.reference.StatusReference.PAYED;

@Service
@Transactional
public class PractitionerBudgetServiceImpl extends BaseServiceImpl<PractitionerBudget, IdDTO<Long>, Long> implements PractitionerBudgetService {

    private final PractitionerBudgetRepository practitionerBudgetRepository;
    private final BudgetItemService budgetItemService;
    private final RenderService renderService;

    @Autowired
    public PractitionerBudgetServiceImpl(PractitionerBudgetRepository practitionerBudgetRepository,
                                         BudgetItemService budgetItemService,
                                         RenderService renderService) {
        super(practitionerBudgetRepository);
        this.practitionerBudgetRepository = practitionerBudgetRepository;
        this.budgetItemService = budgetItemService;
        this.renderService = renderService;
    }

    @Override
    public Optional<PractitionerBudget> calculateBudget(MedicalAuthorizationItem medicalAuthorizationItem) {
        Beneficiary beneficiary = getHolderBeneficiary(medicalAuthorizationItem.getMedicalAuthorization().getBeneficiary());
        if (shouldCalculateBudget(beneficiary, medicalAuthorizationItem.getChargeSubtotal()) &&
                medicalAuthorizationItem.isApproved()) {
            MedicalAuthorization medicalAuthorization = medicalAuthorizationItem.getMedicalAuthorization();
            PractitionerBudget practitionerBudget = createOrRetrieveNotPayedBudget(medicalAuthorization);
            budgetItemService.addBudgetItem(practitionerBudget, medicalAuthorizationItem);
            return Optional.of(practitionerBudgetRepository.save(practitionerBudget));
        }
        return Optional.empty();
    }

    private Beneficiary getHolderBeneficiary(Beneficiary beneficiary) {
        return beneficiary.getRelatedBeneficiary() != null ? beneficiary.getRelatedBeneficiary() : beneficiary;
    }

    private boolean shouldCalculateBudget(Beneficiary beneficiary, BigDecimal chargeValue) {
        return beneficiary.getPaymentMethod().getId().equals(VOLUNTARY.getId()) && chargeValue.intValue() != 0;
    }

    @Override
    public Optional<PractitionerBudget> calculateBudget(MedicalAuthorization medicalAuthorization) {
        Beneficiary beneficiary = getHolderBeneficiary(medicalAuthorization.getBeneficiary());
        if (shouldCalculateBudget(beneficiary, medicalAuthorization.getChargeTotal())) {
            PractitionerBudget practitionerBudget = createOrRetrieveNotPayedBudget(medicalAuthorization);
            List<BudgetItem> newBudgetItems = budgetItemService.calculateBudgetItems(practitionerBudget, medicalAuthorization);
            if (!newBudgetItems.isEmpty())
                return Optional.of(practitionerBudgetRepository.save(practitionerBudget));
        }
        return Optional.empty();
    }

    @Override
    public boolean belongsToPractitioner(long budgetId) {
        UUID practitionerResourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return practitionerBudgetRepository.existsByIdAndPractitionerResourceId(budgetId, practitionerResourceId);
    }

    @Override
    public boolean belongsToMedicalCenter(long budgetId) {
        UUID medicalCenterResourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        return practitionerBudgetRepository.existsByIdAndMedicalCenterResourceId(budgetId, medicalCenterResourceId);
    }

    @Override
    public ByteArrayOutputStream generateReceipt(long budgetId) throws ObjectNotValidException, ObjectNotFoundException {
        PractitionerBudget practitionerBudget = this.findById(budgetId);
        if (!practitionerBudget.getStatus().getId().equals(PAYED.getId()))
            throw new ObjectNotValidException("budget.invalidReceipt");
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("generatedBy", SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("-"));
        templateValues.put("generatedAt", LocalDateTime.now());
        templateValues.put("data", practitionerBudget);
        return renderService.renderPDF("practitioner_budget", templateValues);
    }

    private PractitionerBudget createOrRetrieveNotPayedBudget(MedicalAuthorization medicalAuthorization) {
        Practitioner practitioner = medicalAuthorization.getPractitioner();
        MedicalCenter medicalCenter = medicalAuthorization.getMedicalCenter();
        return practitionerBudgetRepository.findByPractitionerIdAndMedicalCenterIdAndStatusId(practitioner.getId(), medicalCenter.getId(), StatusReference.NOT_PAYED.getId())
                .orElseGet(() -> {
                    PractitionerBudget newBudget = new PractitionerBudget();
                    newBudget.setPractitioner(practitioner);
                    newBudget.setMedicalCenter(medicalCenter);
                    Status notPayed = new Status();
                    notPayed.setId(StatusReference.NOT_PAYED.getId());
                    newBudget.setStatus(notPayed);
                    newBudget.setTotal(new BigDecimal(0));
                    return practitionerBudgetRepository.save(newBudget);
                });
    }

    @Override
    public Optional<Specification<PractitionerBudget>> appendCustomSpecification() {
        if (SecurityUtils.isMedicalCenter())
            return Optional.of((root, query, builder) -> {
                var join = root.join("medicalCenter");
                return builder.equal(join.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
            });
        return Optional.empty();
    }

}
