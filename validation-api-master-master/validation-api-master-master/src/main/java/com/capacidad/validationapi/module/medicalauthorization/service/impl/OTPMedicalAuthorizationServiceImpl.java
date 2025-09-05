package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.medicalauthorization.dto.OTPMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.OTPMedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.reference.AuthorizationTypeReference;
import com.capacidad.validationapi.module.medicalauthorization.repository.OTPMedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.OTPMedicalAuthorizationService;
import com.capacidad.validationapi.module.totp.Authenticator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.EQUAL;

@Log4j2
@Service
public class OTPMedicalAuthorizationServiceImpl extends BaseMedicalAuthorizationServiceImpl<OTPMedicalAuthorization, OTPMedicalAuthorizationDTO> implements OTPMedicalAuthorizationService {

    private final OTPMedicalAuthorizationRepository otpMedicalAuthorizationRepository;
    private final Hashids hashids;
    private final Authenticator authenticator;
    private final Base32 codec32;
    private final BeneficiaryFinder beneficiaryFinder;

    @Autowired
    public OTPMedicalAuthorizationServiceImpl(OTPMedicalAuthorizationRepository repository,
                                              Hashids hashids,
                                              Authenticator authenticator,
                                              BeneficiaryFinder beneficiaryFinder) {
        super(repository);
        this.otpMedicalAuthorizationRepository = repository;
        this.hashids = hashids;
        this.authenticator = authenticator;
        this.codec32 = new Base32();
        this.beneficiaryFinder = beneficiaryFinder;
    }

    @Override
    public OTPMedicalAuthorization initialize(OTPMedicalAuthorizationDTO otpMedicalAuthorizationDTO) throws ObjectNotValidException, ObjectNotFoundException {
        OTPMedicalAuthorization initializedInput = super.initialize(otpMedicalAuthorizationDTO);
        Beneficiary beneficiary = getBeneficiaryFromValidatedOTP(initializedInput);
        initializedInput.setBeneficiary(beneficiary);
        this.validate(initializedInput);
        initializedInput.setAuthorizationType(AuthorizationTypeReference.AUTHORIZATION_TYPE_MANUAL_CODE.getInstance());
        return initializedInput;
    }

    private Beneficiary getBeneficiaryFromValidatedOTP(OTPMedicalAuthorization otpMedicalAuthorization) throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = beneficiaryFinder.findByIdLocked(otpMedicalAuthorization.getBeneficiary().getId());
        String beneficiaryHashKey = hashids.encode(beneficiary.getIdNumber(), beneficiary.getIdNumber());
        String hexKey = Hex.encodeHexString(beneficiaryHashKey.getBytes()).toUpperCase();
        String base32Key = codec32.encodeAsString(hexKey.getBytes()).replace(EQUAL, "");
        boolean isValid = authenticator.authorize(base32Key, Integer.parseInt(otpMedicalAuthorization.getOtp()), Instant.now().toEpochMilli());
        if (!isValid)
            throw new ObjectNotValidException("medicalAuthorization.invalidOTP", otpMedicalAuthorization.getOtp());
        Optional<OTPMedicalAuthorization> usedOtp = otpMedicalAuthorizationRepository.findByOtpAndBeneficiaryId(otpMedicalAuthorization.getOtp(), beneficiary.getId());
        if (usedOtp.isPresent()) {
            Duration duration = Duration.between(usedOtp.get().getCreatedAt(), LocalDateTime.now());
            long minHours = 24;
            if (duration.toHours() <= minHours)
                throw new ObjectNotValidException("medicalAuthorization.otpAlreadyUsed", otpMedicalAuthorization.getOtp(), String.valueOf(24));
        }
        return beneficiary;
    }


}
