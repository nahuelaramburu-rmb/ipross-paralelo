package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalauthorization.dto.*;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.service.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Log4j2
@Transactional(value = "chainedTransactionManager", rollbackFor = Exception.class)
public class MedicalAuthorizationMediatorImpl implements MedicalAuthorizationMediator {

    private final IdMedicalAuthorizationService idMedicalAuthorizationService;
    private final MagstripeMedicalAuthorizationService magstripeMedicalAuthorizationService;
    private final OTPMedicalAuthorizationService otpMedicalAuthorizationService;
    private final QRMedicalAuthorizationService qrMedicalAuthorizationService;

    @Autowired
    public MedicalAuthorizationMediatorImpl(IdMedicalAuthorizationService idMedicalAuthorizationService,
                                            MagstripeMedicalAuthorizationService magstripeMedicalAuthorizationService,
                                            OTPMedicalAuthorizationService otpMedicalAuthorizationService,
                                            QRMedicalAuthorizationService qrMedicalAuthorizationService) {
        this.idMedicalAuthorizationService = idMedicalAuthorizationService;
        this.magstripeMedicalAuthorizationService = magstripeMedicalAuthorizationService;
        this.otpMedicalAuthorizationService = otpMedicalAuthorizationService;
        this.qrMedicalAuthorizationService = qrMedicalAuthorizationService;
    }

    @Override
    public MedicalAuthorizationProjection apply(IdMedicalAuthorizationDTO idMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        return applyStrategy(idMedicalAuthorizationDTO, idMedicalAuthorizationService);
    }

    @Override
    public MedicalAuthorizationProjection apply(MagstripeMedicalAuthorizationDTO magstripeMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        return applyStrategy(magstripeMedicalAuthorizationDTO, magstripeMedicalAuthorizationService);
    }

    @Override
    public MedicalAuthorizationProjection apply(OTPMedicalAuthorizationDTO otpMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        return applyStrategy(otpMedicalAuthorizationDTO, otpMedicalAuthorizationService);
    }

    @Override
    public MedicalAuthorizationProjection apply(QRMedicalAuthorizationDTO qrMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        return applyStrategy(qrMedicalAuthorizationDTO, qrMedicalAuthorizationService);
    }

    private <T extends MedicalAuthorization, D extends MedicalAuthorizationDTO, B extends BaseMedicalAuthorizationService<T, D>> MedicalAuthorizationProjection applyStrategy(D medicalAuthorizationDTO, B baseMedicalAuthorizationService) throws ObjectNotValidException, ObjectNotFoundException {
        T initMedAuth = baseMedicalAuthorizationService.initialize(medicalAuthorizationDTO);
        baseMedicalAuthorizationService.preProcess(initMedAuth);
        T persisted = baseMedicalAuthorizationService.persist(initMedAuth);
        baseMedicalAuthorizationService.approve(persisted);
        return baseMedicalAuthorizationService.postProcess(persisted);
    }


}
