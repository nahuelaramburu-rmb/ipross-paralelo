package com.capacidad.validationapi.prescription.integration;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.prescription.model.Prescription;

import java.util.Collection;
import java.util.Optional;

public interface PrescriptionIntegration {

    void savePrescription(Prescription prescription) throws ObjectNotValidException, ObjectNotFoundException;

    void savePrescriptions(Collection<Prescription> prescriptions) throws ObjectNotValidException, ObjectNotFoundException;

    void cancelPrescription(Prescription prescription);

    void syncPrescriptionsStatus(Collection<Prescription> prescriptions, Status newStatus);

    Optional<Prescription> findBeneficiaryValidatedPrescription(Beneficiary beneficiary, String id);

}
