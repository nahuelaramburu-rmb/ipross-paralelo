package com.capacidad.validationapi.module.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ExtendedJpaRepository<T, I extends Serializable> extends JpaRepository<T, I>, JpaSpecificationExecutorWithProjection<T> {

    @Transactional
    @Query("update #{#entityName} e set e.deleted = true, e.modifiedAt = current_timestamp, e.deletionToken = function('uuid_generate_v4') where e.id = :id")
    @Modifying
    void softDelete(@Param("id") I id);

    boolean existsById(I id);

    long countAllByIdIn(Collection<I> id);

    <P> Optional<P> findProjectedById(I id, Class<P> type);

    <P> List<P> findAllProjectedBy(Class<P> type);

    <P> Page<P> findAllProjectedBy(Class<P> type, Pageable pageable);

}
