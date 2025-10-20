package com.capacidad.validationapi.module.person.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.person.model.Person;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BasePersonRepository<T extends Person, I extends Serializable> extends ExtendedJpaRepository<T, I> {
}
