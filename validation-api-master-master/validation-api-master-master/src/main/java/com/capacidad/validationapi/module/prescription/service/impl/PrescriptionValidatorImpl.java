package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryService;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationValidator;
import com.capacidad.validationapi.module.medicalcenter.service.MedicalCenterService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.service.PrescriptionRestrictionService;
import com.capacidad.validationapi.module.prescription.service.PrescriptionValidator;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionValidatorImpl implements PrescriptionValidator {

    private final MedicalCenterService medicalCenterService;
    private final MedicalAuthorizationValidator medicalAuthorizationValidator;
    private final PractitionerService practitionerService;
    private final BeneficiaryService beneficiaryService;
    private final PrescriptionRestrictionService prescriptionRestrictionService;

    public PrescriptionValidatorImpl(MedicalCenterService medicalCenterService,
                                     MedicalAuthorizationValidator medicalAuthorizationValidator,
                                     PractitionerService practitionerService,
                                     BeneficiaryService beneficiaryService,
                                     PrescriptionRestrictionService prescriptionRestrictionService) {
        this.medicalCenterService = medicalCenterService;
        this.medicalAuthorizationValidator = medicalAuthorizationValidator;
        this.practitionerService = practitionerService;
        this.beneficiaryService = beneficiaryService;
        this.prescriptionRestrictionService = prescriptionRestrictionService;
    }

    @Override
    public void validate(Prescription prescription) throws ObjectNotValidException, ObjectNotFoundException {
        validatePrescriptionItems(prescription);
        if (SecurityUtils.isMedicalCenter()) {
            prescription.setMedicalCenter(medicalCenterService.getAuthMedicalCenter());
            Practitioner practitioner = practitionerService.findById(prescription.getPractitioner().getId());
            prescription.setPractitioner(practitioner);
            validateTransactionKey(prescription);
        }
        if (SecurityUtils.isPractitioner())
            prescription.setPractitioner(practitionerService.getAuthPractitioner());
        prescription.setBeneficiary(beneficiaryService.findById(prescription.getBeneficiary().getId()));
        medicalAuthorizationValidator.validatePractitionerStatus(prescription.getPractitioner());
        medicalAuthorizationValidator.validateBeneficiaryStatus(prescription.getBeneficiary());
        prescriptionRestrictionService.validateSpecialties(prescription.getPractitioner().getMedicalSpecialties());
        if (prescription.getMedicalCenter() != null)
            medicalAuthorizationValidator.validatePractitionerMedicalCenter(prescription.getPractitioner(), prescription.getMedicalCenter());
    }

    private void validatePrescriptionItems(Prescription prescription) throws ObjectNotValidException {
        if (prescription.getPrescriptionItems().size() > 2)
            throw new ObjectNotValidException("prescription.maxPrescriptionItems", "2");
    }

    private void validateTransactionKey(Prescription prescription) throws ObjectNotValidException {
        if (prescription.getTransactionKey() == null || !prescription.getPractitioner().getTransactionKey().equals(prescription.getTransactionKey()))
            throw new ObjectNotValidException("prescription.invalidTransactionKey");
    }

    @Override
    public void validateFromMedicalAuthorization(Prescription prescription) throws ObjectNotValidException {
        validatePrescriptionItems(prescription);
        if (SecurityUtils.isMedicalCenter())
            validateTransactionKey(prescription);
        prescriptionRestrictionService.validateSpecialties(prescription.getPractitioner().getMedicalSpecialties());
    }
}
