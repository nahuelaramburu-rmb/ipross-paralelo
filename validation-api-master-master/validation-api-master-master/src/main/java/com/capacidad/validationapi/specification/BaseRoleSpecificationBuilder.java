package com.capacidad.validationapi.specification;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.Optional;

public abstract class BaseRoleSpecificationBuilder<T extends BaseTenantEntity<I>, I extends Serializable> implements RoleSpecificationBuilder<T, I> {

    @Override
    public Optional<Specification<T>> buildSpecification() {
        if (SecurityUtils.isBeneficiary())
            return buildSpecificationForBeneficiaries();
        if (SecurityUtils.isPractitioner())
            return buildSpecificationForPractitioner();
        if (SecurityUtils.isMedicalCenter())
            return buildSpecificationForMedicalCenter();
        if (SecurityUtils.isOrganization())
            return buildSpecificationForOrganizations();
        return Optional.empty();
    }

    @Override
    public Optional<Specification<T>> buildSpecificationForBeneficiaries() {
        return Optional.empty();
    }

    @Override
    public Optional<Specification<T>> buildSpecificationForPractitioner() {
        return Optional.empty();
    }

    @Override
    public Optional<Specification<T>> buildSpecificationForMedicalCenter() {
        return Optional.empty();
    }

    @Override
    public Optional<Specification<T>> buildSpecificationForOrganizations() {
        return Optional.empty();
    }

}
