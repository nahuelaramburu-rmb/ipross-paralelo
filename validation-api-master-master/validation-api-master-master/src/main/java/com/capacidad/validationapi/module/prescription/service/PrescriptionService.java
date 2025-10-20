package com.capacidad.validationapi.module.prescription.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionDTO;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionProjection;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Set;

public interface PrescriptionService extends BaseService<Prescription, PrescriptionDTO, Long> {

    void createFromMedicalAuthorization(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException;

    void createAll(Set<PrescriptionDTO> prescriptionDtos) throws ObjectNotValidException, ObjectNotFoundException;

    boolean existsByAuthBeneficiaryOrRelative(long prescriptionId);

    boolean existsByAuthPractitioner(long prescriptionId);

    boolean existsByAuthMedicalCenter(long prescriptionId);

    void save(Prescription prescription);

    void saveAll(Collection<Prescription> prescriptions);

    PrescriptionProjection cancelPrescription(long prescriptionId, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    void syncStatus();

    ByteArrayOutputStream generateReceipt(long prescriptionId) throws ObjectNotFoundException, ObjectNotValidException;

    PrescriptionProjection.Integration findPrescription(String beneficiaryCode, Long exchangeId) throws ObjectNotFoundException, ObjectNotValidException;

}
