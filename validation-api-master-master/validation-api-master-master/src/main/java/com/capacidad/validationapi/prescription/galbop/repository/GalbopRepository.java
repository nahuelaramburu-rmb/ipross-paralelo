package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescription;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionDetail;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionNumeration;
import com.capacidad.validationapi.prescription.galbop.projection.GalbopBeneficiaryProjection;
import com.capacidad.validationapi.prescription.integration.ProductIntegrationProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GalbopRepository {

    List<GalbopBeneficiaryProjection> findBeneficiaryGroupedPlans(String beneficiaryCode);

    GalbopPrescriptionNumeration getLastNumeration() throws ObjectNotValidException;

    void updateNumeration(GalbopPrescriptionNumeration newNumeration) throws ObjectNotValidException;

    long getNextPrescriptionId() throws ObjectNotValidException;

    GalbopPrescription savePrescription(GalbopPrescription galbopPrescription) throws ObjectNotValidException;

    GalbopPrescriptionDetail savePrescriptionDetail(GalbopPrescriptionDetail galbopPrescriptionDetail) throws ObjectNotValidException;

    Set<ProductIntegrationProjection> findProductsByNameAndVademecumIds(String name, int[] vademecumIds);

    Set<ProductIntegrationProjection> findProductsInPrescriptionDetail(GalbopPrescriptionDetail galbopPrescriptionDetail);

    void updatePrescriptionDetail(Set<String> prescriptionNumbers, String status, LocalDateTime statusDate);

    Set<GalbopPrescriptionDetail> findAllPrescriptionDetails(String status, Set<String> prescriptionNumbers);

    Optional<GalbopPrescriptionDetail> findValidatedPrescriptionDetail(String beneficiaryCode, Long planId, String prescriptionNumber);

}
