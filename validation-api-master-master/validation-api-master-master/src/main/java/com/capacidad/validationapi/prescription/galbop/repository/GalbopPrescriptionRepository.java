package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescription;
import com.capacidad.validationapi.prescription.galbop.projection.GalbopPrescriptionIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GalbopPrescriptionRepository extends JpaRepository<GalbopPrescription, Long> {

    GalbopPrescriptionIdProjection findTopByOrderByPrescriptionIdDesc();

}
