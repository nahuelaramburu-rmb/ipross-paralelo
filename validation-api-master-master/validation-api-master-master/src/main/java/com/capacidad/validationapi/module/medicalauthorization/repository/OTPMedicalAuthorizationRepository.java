package com.capacidad.validationapi.module.medicalauthorization.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.medicalauthorization.model.OTPMedicalAuthorization;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@TenantFilter
public interface OTPMedicalAuthorizationRepository extends BaseMedicalAuthorizationRepository<OTPMedicalAuthorization, Long> {

    Optional<OTPMedicalAuthorization> findByOtpAndBeneficiaryId(String otp, long beneficiaryId);

}
