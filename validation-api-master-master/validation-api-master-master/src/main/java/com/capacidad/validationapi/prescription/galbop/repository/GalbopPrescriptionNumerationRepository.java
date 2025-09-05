package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionNumeration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GalbopPrescriptionNumerationRepository extends JpaRepository<GalbopPrescriptionNumeration, Long> {

    GalbopPrescriptionNumeration findTopByOrderByNumerationDesc();

    @Query("update GalbopPrescriptionNumeration n set n.numeration = ?1")
    @Modifying
    void updateNumeration(Long newNumeration);

}
