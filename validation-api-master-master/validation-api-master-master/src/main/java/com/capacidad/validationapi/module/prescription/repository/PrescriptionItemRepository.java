package com.capacidad.validationapi.module.prescription.repository;


import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionItemRepository extends ExtendedJpaRepository<PrescriptionItem, Long> {
}
