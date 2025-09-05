package com.capacidad.validationapi.module.insuranceplan.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.insuranceplan.dto.InsurancePlanDTO;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlanType;

import javax.persistence.EntityManager;
import java.util.List;

public interface InsurancePlanService extends BaseService<InsurancePlan, InsurancePlanDTO, Long> {

    List<InsurancePlanType> getAllInsurancePlanTypes();

    List<InsurancePlan> findAllInsurancePlansTypedQuery(EntityManager entityManager);

}
