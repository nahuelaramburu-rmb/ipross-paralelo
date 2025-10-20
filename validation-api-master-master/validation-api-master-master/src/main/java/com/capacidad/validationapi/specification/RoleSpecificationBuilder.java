package com.capacidad.validationapi.specification;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.Optional;

public interface RoleSpecificationBuilder<T extends BaseTenantEntity<I>, I extends Serializable> {

    Optional<Specification<T>> buildSpecification();

    Optional<Specification<T>> buildSpecificationForBeneficiaries();

    Optional<Specification<T>> buildSpecificationForPractitioner();

    Optional<Specification<T>> buildSpecificationForMedicalCenter();

    Optional<Specification<T>> buildSpecificationForOrganizations();

}
