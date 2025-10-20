package com.capacidad.validationapi.prescription.integration;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class DefaultPrescriptionServiceImpl implements MedicinesIntegration, PrescriptionIntegration {
    @Override
    public ProductWrapperDTO getProducts(String name, String beneficiaryCode) {
        //TODO: getProducts(String name, String beneficiaryCode)
        return new ProductWrapperDTO(Collections.emptySet(), 0);
    }

    @Override
    public void savePrescription(Prescription prescription) {
        //TODO: savePrescription(Prescription prescription)
    }

    @Override
    public void savePrescriptions(Collection<Prescription> prescriptions) {
        //TODO: savePrescriptions(Set<Prescription> prescriptions)
    }

    @Override
    public void cancelPrescription(Prescription prescription) {
        //TODO: cancelPrescription(Prescription prescription)
    }

    @Override
    public void syncPrescriptionsStatus(Collection<Prescription> prescriptions, Status newStatus) {
        //TODO: syncPrescriptions(Collection<Prescription> prescriptions, Status newStatus)
    }

    @Override
    public Optional<Prescription> findBeneficiaryValidatedPrescription(Beneficiary beneficiary, String id) {
        //TODO: findBeneficiaryValidatedPrescription(Beneficiary beneficiary, String id)
        return Optional.empty();
    }
}
