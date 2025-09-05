package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ModelConstants;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.dto.ExpirationDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.Expiration;
import com.capacidad.validationapi.module.beneficiary.projection.ExpirationProjection;
import com.capacidad.validationapi.module.beneficiary.repository.ExpirationRepository;
import com.capacidad.validationapi.module.beneficiary.service.ExpirationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Log4j2
@Service
public class ExpirationServiceImpl extends BaseServiceImpl<Expiration, ExpirationDTO, Long> implements ExpirationService {

    private final ExpirationRepository expirationRepository;

    @Autowired
    public ExpirationServiceImpl(ExpirationRepository repository) {
        super(repository);
        this.expirationRepository = repository;
    }

    @Override
    public void validate(Expiration object) throws ObjectAlreadyExistsException {
        if (expirationRepository.existsByExpirationDateGreaterThanEqual(LocalDateTime.now()))
            throw new ObjectAlreadyExistsException("expiration.alreadyExistsForDates");
    }

    @Override
    public Expiration create(ExpirationDTO dto, Beneficiary beneficiary) throws ObjectNotValidException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        Expiration object = this.mapDtoToInput(dto);
        object.setBeneficiary(beneficiary);
        this.validate(object);
        Expiration objectResult = expirationRepository.save(object);
        log.info("create - void: {}({})", object.getClass(), object);
        return objectResult;
    }

    @Override
    public Page<ExpirationProjection> getBeneficiaryExpirations(long beneficiaryId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, ModelConstants.CREATED_AT);
        return expirationRepository.findAllProjectedByBeneficiaryId(beneficiaryId, pageRequest);
    }

    @Override
    public long findExpirationAndGetBeneficiaryId(long expirationId) throws ObjectNotFoundException {
        return expirationRepository.findBeneficiaryIdProjectionById(expirationId)
                .map(i -> i.getBeneficiary().getId())
                .orElseThrow(() -> new ObjectNotFoundException("expiration.notFound"));
    }
}
