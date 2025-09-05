package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.medicalauthorization.dto.IdMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalauthorization.repository.MedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.IdMedicalAuthorizationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class IdMedicalAuthorizationServiceImpl extends BaseMedicalAuthorizationServiceImpl<MedicalAuthorization, IdMedicalAuthorizationDTO> implements IdMedicalAuthorizationService {

    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public IdMedicalAuthorizationServiceImpl(MedicalAuthorizationRepository repository,
                                             BeneficiaryFinder beneficiaryFinder) {
        super(repository);
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public MedicalAuthorization initialize(IdMedicalAuthorizationDTO medicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        MedicalAuthorization initializedInput = super.initialize(medicalAuthorizationDTO);
        Beneficiary mappedBeneficiary = initializedInput.getBeneficiary();
        initializedInput.setBeneficiary(beneficiaryFinder.findBeneficiaryLocked(mappedBeneficiary.getIdNumber(), mappedBeneficiary.getIdType()));
        this.validate(initializedInput);
        initializedInput.setAuthorizationType(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_ID_NUMBER.getInstance());
        return initializedInput;
    }
}
