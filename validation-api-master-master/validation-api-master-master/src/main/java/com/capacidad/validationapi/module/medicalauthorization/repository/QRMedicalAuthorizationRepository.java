package com.capacidad.validationapi.module.medicalauthorization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.medicalauthorization.model.QRMedicalAuthorization;
import org.springframework.stereotype.Repository;

@Repository
@TenantFilter
public interface QRMedicalAuthorizationRepository extends BaseMedicalAuthorizationRepository<QRMedicalAuthorization, Long> {
    boolean existsByEncryptedQrKey(String encryptedQrKey);
}
