package com.capacidad.validationapi.module.exportprocessor.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@TenantFilter
public interface FileDownloadKeyRepository extends ExtendedJpaRepository<FileDownloadKey, Long> {

    @TenantFilter(active = false)
    Set<FileDownloadKey> findAllBy();

}
