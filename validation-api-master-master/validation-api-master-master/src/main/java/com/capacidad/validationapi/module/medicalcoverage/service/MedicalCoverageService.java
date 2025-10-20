package com.capacidad.validationapi.module.medicalcoverage.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.dto.MedicalCoverageDTO;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageProjection;

import java.util.List;
import java.util.Set;

public interface MedicalCoverageService extends BaseService<MedicalCoverage, MedicalCoverageDTO, Long> {

    MedicalCoverage create(MedicalCoverageDTO dto, InsurancePlan insurancePlan) throws ObjectNotFoundException, ObjectNotValidException;

    Set<MedicalCoverageProjection> getMedicalCoverages(long insurancePlanId);

    List<IdAndNameOnlyProjection> getAllChargeTypes();

    MedicalCoverage findApplicableCoverage(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException;

    void applyMedicalCoverageToMedicalAuthorizationItem(MedicalAuthorizationItem medicalAuthorizationItem) throws ObjectNotFoundException;

    void calculateAuthorizationItemCharges(MedicalAuthorizationItem medicalAuthorizationItem);

}
