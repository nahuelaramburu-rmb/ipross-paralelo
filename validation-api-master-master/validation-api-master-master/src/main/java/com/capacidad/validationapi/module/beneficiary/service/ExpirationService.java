package com.capacidad.validationapi.module.beneficiary.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.beneficiary.dto.ExpirationDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.Expiration;
import com.capacidad.validationapi.module.beneficiary.projection.ExpirationProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpirationService extends BaseService<Expiration, ExpirationDTO, Long> {

    Expiration create(ExpirationDTO dto, Beneficiary beneficiary) throws ObjectNotValidException;

    Page<ExpirationProjection> getBeneficiaryExpirations(long beneficiaryId, Pageable pageable);

    long findExpirationAndGetBeneficiaryId(long expirationId) throws ObjectNotFoundException;

}
