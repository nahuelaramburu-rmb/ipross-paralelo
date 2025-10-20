package com.capacidad.validationapi.module.prescription.config.security;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.practitioner.config.security.PractitionerChecker;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionDTO;
import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Component
public class PrescriptionChecker {

    private final PrescriptionService prescriptionService;
    private final PractitionerChecker practitionerChecker;

    @Autowired
    public PrescriptionChecker(PrescriptionService prescriptionService,
                               PractitionerChecker practitionerChecker) {
        this.prescriptionService = prescriptionService;
        this.practitionerChecker = practitionerChecker;
    }

    public boolean hasAccessToPrescription(long prescriptionId) {
        if (SecurityUtils.isBeneficiary())
            return prescriptionService.existsByAuthBeneficiaryOrRelative(prescriptionId);
        if (SecurityUtils.isPractitioner())
            return prescriptionService.existsByAuthPractitioner(prescriptionId);
        if (SecurityUtils.isMedicalCenter())
            return prescriptionService.existsByAuthMedicalCenter(prescriptionId);
        return SecurityUtils.isHighRankingAuthority();
    }

    public boolean allPrescriptionsBelongsToSameValidPractitioner(Set<PrescriptionDTO> input) {
        Map<Long, List<PrescriptionDTO>> mappedInput = input.stream().collect(Collectors.groupingBy(o -> o.getPractitioner().getId()));
        Set<Long> keySet = mappedInput.keySet();
        if (keySet.size() != 1)
            return false;
        return practitionerChecker.hasAccessToPractitioner(keySet.iterator().next());
    }

}
