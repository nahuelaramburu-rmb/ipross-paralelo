package com.capacidad.validationapi.module.medicalauthorization.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;

public interface MedicalAuthorizationValidator {

    Nomenclator getValidatedNomenclator(Practitioner practitioner, long nomenclatorId) throws ObjectNotValidException, ObjectNotFoundException;

    void validateMaxQuantity(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotValidException;

    void validatePractitionerStatus(Practitioner practitioner) throws ObjectNotValidException;

    void validatePractitionerMedicalCenter(Practitioner practitioner, MedicalCenter medicalCenter) throws ObjectNotValidException;

    void validateBeneficiaryStatus(Beneficiary beneficiary) throws ObjectNotValidException;

    void validateAndSetPetitioner(MedicalAuthorization medicalAuthorization) throws ObjectNotFoundException;

}
