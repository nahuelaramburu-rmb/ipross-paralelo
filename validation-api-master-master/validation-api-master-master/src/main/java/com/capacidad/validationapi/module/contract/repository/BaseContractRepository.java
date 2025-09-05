package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.contract.model.Contract;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseContractRepository<T extends Contract, I extends Serializable> extends ExtendedJpaRepository<T, I> {
}
