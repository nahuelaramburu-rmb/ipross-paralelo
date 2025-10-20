package com.capacidad.validationapi.module.company.service;

import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.company.dto.CompanyDTO;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.company.projection.CompanyProjection;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

public interface CompanyService extends BaseService<Company, CompanyDTO, Long> {

    Set<CompanyProjection> getCompanies(String name);

    List<Company> findAllCompaniesTypedQuery(EntityManager entityManager);

}
