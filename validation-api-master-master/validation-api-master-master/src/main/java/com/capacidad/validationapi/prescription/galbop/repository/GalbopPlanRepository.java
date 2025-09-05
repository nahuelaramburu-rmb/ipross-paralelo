package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.validationapi.prescription.galbop.model.GalbopPlan;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPlanEmbeddedId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GalbopPlanRepository extends JpaRepository<GalbopPlan, GalbopPlanEmbeddedId> {
}
