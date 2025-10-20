package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationValidator;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MedicalAuthorizationValidatorImpl implements MedicalAuthorizationValidator {

    private final PractitionerService practitionerService;
    private final NomenclatorService nomenclatorService;

    @Autowired
    public MedicalAuthorizationValidatorImpl(PractitionerService practitionerService,
                                             NomenclatorService nomenclatorService) {
        this.practitionerService = practitionerService;
        this.nomenclatorService = nomenclatorService;
    }

    @Override
    public Nomenclator getValidatedNomenclator(Practitioner practitioner, long nomenclatorId) throws ObjectNotValidException, ObjectNotFoundException {
        Nomenclator nomenclator = this.nomenclatorService.findById(nomenclatorId);
        if (!this.practitionerService.canPerformMedicalPractice(practitioner, nomenclator.getMedicalPractice()))
            throw new ObjectNotValidException("medicalAuthorization.invalidSpecialty", nomenclator.getNomenclatorCode());
        return nomenclator;
    }

    @Override
    public void validateMaxQuantity(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException {
        Nomenclator nomenclator = medicalAuthorizationItem.getNomenclator();
        Integer maxInTransaction = nomenclator.getNomenclatorConfig().getMaxInTransaction();
        if (medicalAuthorizationItem.getQuantity() > maxInTransaction)
            throw new ObjectNotValidException("medicalAuthorization.itemMaxQuantityValidation", nomenclator.getNomenclatorCode(), maxInTransaction.toString());
    }

    @Override
    public void validatePractitionerStatus(Practitioner practitioner) throws ObjectNotValidException {
        if (!practitioner.getStatus().getId().equals(StatusReference.AVAILABLE.getId()))
            throw new ObjectNotValidException("medicalAuthorization.disabledPractitioner");
    }

    @Override
    public void validatePractitionerMedicalCenter(Practitioner practitioner, MedicalCenter medicalCenter) throws ObjectNotValidException {
        if (!this.practitionerService.belongsToMedicalCenter(
                practitioner, medicalCenter))
            throw new ObjectNotValidException(
                    "medicalAuthorization.practitionerInvalidMedicalCenter");
    }

    @Override
    public void validateBeneficiaryStatus(Beneficiary beneficiary) throws ObjectNotValidException {
        if (!beneficiary.hasActiveHealthCoverage())
            throw new ObjectNotValidException("beneficiary.noCoverageId", beneficiary.getId().toString());
    }

    @Override
    public void validateAndSetPetitioner(MedicalAuthorization medicalAuthorization) throws ObjectNotFoundException {
        if (medicalAuthorization.getPetitioner() == null)
            medicalAuthorization.setPetitioner(medicalAuthorization.getPractitioner());
        else
            medicalAuthorization.setPetitioner(practitionerService.findById(medicalAuthorization.getPetitioner().getId()));
    }

}
