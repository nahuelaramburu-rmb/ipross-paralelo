package com.capacidad.validationapi.module.beneficiary.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.beneficiary.model.BeneficiaryCategory;
import com.capacidad.validationapi.module.general.dto.NameDTO;

import javax.persistence.EntityManager;
import java.util.List;

public interface BeneficiaryCategoryService extends BaseService<BeneficiaryCategory, NameDTO, Long> {

    List<BeneficiaryCategory> findAllBeneficiaryCategoriesTypedQuery(EntityManager entityManager);

}
