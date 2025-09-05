package com.capacidad.identityservice.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface ExtendedRepository<T, I extends Serializable> extends JpaRepository<T, I>, JpaSpecificationExecutorWithProjection<T> {
}

