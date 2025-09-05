package com.capacidad.validationapi.module.medicalauthorization.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseMedicalAuthorizationRepository<T extends MedicalAuthorization, I extends Serializable> extends ExtendedJpaRepository<T, I> {
}
