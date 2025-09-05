package com.capacidad.validationapi.module.premedicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorizationItem;
import com.capacidad.validationapi.module.premedicalauthorization.service.PreMedicalAuthorizationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;

@Service
public class PreMedicalAuthorizationValidatorImpl implements PreMedicalAuthorizationValidator {

    private final Utils utils;

    @Autowired
    public PreMedicalAuthorizationValidatorImpl(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void validate(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException {
        PreMedicalAuthorization preMedicalAuthorization = medicalAuthorization.getPreMedicalAuthorization();
        validateStatus(preMedicalAuthorization);
        validateExpiration(preMedicalAuthorization);
        validateBeneficiary(preMedicalAuthorization, medicalAuthorization);
        validateItems(preMedicalAuthorization, medicalAuthorization);
    }


    @Override
    public void validateStatus(PreMedicalAuthorization preMedicalAuthorization) throws ObjectNotValidException {
        if (!preMedicalAuthorization.isActive())
            throw new ObjectNotValidException("preMedicalAuthorization.invalidStatus", preMedicalAuthorization.getStatus().getName());
    }

    private void validateExpiration(PreMedicalAuthorization preMedicalAuthorization) throws ObjectNotValidException {
        if (LocalDate.now().isAfter(preMedicalAuthorization.getExpirationDate()))
            throw new ObjectNotValidException("preMedicalAuthorization.expired", preMedicalAuthorization.getExpirationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void validateBeneficiary(PreMedicalAuthorization preMedicalAuthorization, MedicalAuthorization medicalAuthorization) throws ObjectNotValidException {
        if (!preMedicalAuthorization.getBeneficiary().getId().equals(medicalAuthorization.getBeneficiary().getId()))
            throw new ObjectNotValidException("preMedicalAuthorization.invalidBeneficiary");
    }

    private void validateItems(PreMedicalAuthorization preMedicalAuthorization, MedicalAuthorization medicalAuthorization) throws ObjectNotValidException {
        if (preMedicalAuthorization.getPreMedicalAuthorizationItems().size() < medicalAuthorization.getMedicalAuthorizationItems().size())
            throw new ObjectNotValidException("preMedicalAuthorization.invalidItemAmount");
        Map<Long, PreMedicalAuthorizationItem> validationMap = preMedicalAuthorization.getPreMedicalAuthorizationItems().stream()
                .collect(Collectors.toMap(e -> e.getNomenclator().getId(), Function.identity()));
        medicalAuthorization.getMedicalAuthorizationItems()
                .forEach(throwingConsumer(i -> {
                    Nomenclator nomenclator = i.getNomenclator();
                    if (!validationMap.containsKey(nomenclator.getId()))
                        throw new ObjectNotFoundException("preMedicalAuthorization.itemDoesNotExists", nomenclator.getNomenclatorCode());
                    PreMedicalAuthorizationItem preMedicalAuthorizationItem = validationMap.get(nomenclator.getId());
                    validateItem(preMedicalAuthorizationItem, i);
                }));
    }

    private void validateItem(PreMedicalAuthorizationItem preMedicalAuthorizationItem, MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        validateItemAuditing(preMedicalAuthorizationItem);
        if (medicalAuthorizationItem.isPending())
            preMedicalAuthorizationItem.setAuditing(true);
        if (medicalAuthorizationItem.isApproved())
            determineItemConsumption(preMedicalAuthorizationItem, medicalAuthorizationItem);
    }

    private void validateItemAuditing(PreMedicalAuthorizationItem preMedicalAuthorizationItem) throws ObjectNotValidException {
        if (Boolean.TRUE.equals(preMedicalAuthorizationItem.getAuditing()))
            throw new ObjectNotValidException("preMedicalAuthorization.itemAuditing");
    }

    @Override
    public void determineItemConsumption(PreMedicalAuthorizationItem preMedicalAuthorizationItem, MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        int quantityDiff = preMedicalAuthorizationItem.getRemaining() - medicalAuthorizationItem.getQuantity();
        if (quantityDiff < 0)
            throw new ObjectNotValidException("preMedicalAuthorization.itemAlreadyConsumed", medicalAuthorizationItem.getNomenclator().getNomenclatorCode(),
                    preMedicalAuthorizationItem.getRemaining().toString());
        preMedicalAuthorizationItem.setRemaining(quantityDiff);
    }

    @Override
    public Status determineStatus(PreMedicalAuthorization preMedicalAuthorization) {
        if (preMedicalAuthorization.isCancelled())
            return preMedicalAuthorization.getStatus();
        long consumedAmount = preMedicalAuthorization.getPreMedicalAuthorizationItems().stream()
                .filter(i -> i.getRemaining() == 0).count();
        if (consumedAmount == preMedicalAuthorization.getPreMedicalAuthorizationItems().size())
            return utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_CONSUMED.getId());
        if (preMedicalAuthorization.isExpired())
            return utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId());
        return utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());
    }
}
