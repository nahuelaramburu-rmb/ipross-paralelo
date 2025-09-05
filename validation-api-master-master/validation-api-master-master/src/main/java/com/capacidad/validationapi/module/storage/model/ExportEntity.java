package com.capacidad.validationapi.module.storage.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityGraph;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class ExportEntity<T extends BaseEntity<I>, I extends Serializable> {

    private ExtendedJpaRepository<T, I> repository;
    private OutputStream outputStream;
    private Optional<Specification<T>> searchSpec;
    private Sort sort;
    private String[] headerNames;
    private EntityGraph<T> entityGraph;

}
