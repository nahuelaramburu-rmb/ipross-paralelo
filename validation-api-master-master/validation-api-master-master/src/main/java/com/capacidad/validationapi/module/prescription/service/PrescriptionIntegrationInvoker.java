package com.capacidad.validationapi.module.prescription.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.prescription.model.Prescription;

import java.util.Collection;
import java.util.Optional;

public interface PrescriptionIntegrationInvoker {

    void invokeCreation(Collection<Prescription> prescriptions) throws ObjectNotValidException, ObjectNotFoundException;

    void invokeCreation(Prescription prescription) throws ObjectNotValidException, ObjectNotFoundException;

    void invokeCancellation(Prescription prescription) throws ObjectNotValidException;

    void invokeStatusSynchronization(String serviceName, Collection<Prescription> prescriptions, Status newStatus) throws ObjectNotValidException;

    Optional<Prescription> invokeFindPrescription(Beneficiary beneficiary, String exchangeId) throws ObjectNotValidException;

}
