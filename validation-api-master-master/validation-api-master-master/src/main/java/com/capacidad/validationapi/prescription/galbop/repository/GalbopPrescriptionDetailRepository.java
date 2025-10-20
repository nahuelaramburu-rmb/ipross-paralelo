package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GalbopPrescriptionDetailRepository extends JpaRepository<GalbopPrescriptionDetail, Long> {

    @Query("update GalbopPrescriptionDetail d set d.status = ?2, d.statusDate = ?3 where d.prescriptionNumber in ?1")
    @Modifying
    void updatePrescriptionDetail(Set<String> prescriptionNumbers, String status, LocalDateTime statusDate);

    Set<GalbopPrescriptionDetail> findAllByStatusAndPrescriptionNumberIn(String status, Set<String> prescriptionNumbers);

    @Query(value = "select gd from GalbopPrescriptionDetail gd " +
            "join GalbopPrescription gp on (gd.id.prescriptionId = gp.id.prescriptionId)  " +
            "where gd.prescriptionNumber like ?1 " +
            "and gd.dateFrom <= ?2 and gd.dateTo >= ?2 " +
            "and (gd.status like '' or gd.status is null) " +
            "and (gp.status like '' or gp.status is null) " +
            "and gp.id.planId = ?3 " +
            "and gp.id.beneficiaryCode like ?4")
    Optional<GalbopPrescriptionDetail> findValidatedPrescriptionDetail
            (String prescriptionNumber, LocalDate now, Long planId, String beneficiaryCode);

}
