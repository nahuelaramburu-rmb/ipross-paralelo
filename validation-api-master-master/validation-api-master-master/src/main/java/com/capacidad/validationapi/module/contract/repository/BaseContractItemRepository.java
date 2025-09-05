package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseContractItemRepository<T extends ContractItem, I extends Serializable> extends ExtendedJpaRepository<T, I> {
}
