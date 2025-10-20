package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionRestrictionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrescriptionRestrictionServiceImplTest {

    @Mock
    private PrescriptionRestrictionRepository prescriptionRestrictionRepository;

    @InjectMocks
    private PrescriptionRestrictionServiceImpl prescriptionRestrictionService;

    @Test
    public void validateSpecialtiesFailsWhenRestrictedSpecialties() {
        Set<MedicalSpecialty> medicalSpecialties = new HashSet<>();
        MedicalSpecialty medicalSpecialty = new MedicalSpecialty();
        MedicalSpecialty medicalSpecialty1 = new MedicalSpecialty();
        medicalSpecialties.add(medicalSpecialty);
        medicalSpecialties.add(medicalSpecialty1);

        when(prescriptionRestrictionRepository.countAllByMedicalSpecialtyIn(medicalSpecialties)).thenReturn(medicalSpecialties.size());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> prescriptionRestrictionService.validateSpecialties(medicalSpecialties));

        assertThat(exception.getMessage()).isEqualTo("prescriptionRestriction.invalidSpecialty");
    }

    @Test
    public void validateSpecialtiesDoNotFailsWhenEmptyRestrictions() throws ObjectNotValidException {
        Set<MedicalSpecialty> medicalSpecialties = new HashSet<>();
        MedicalSpecialty medicalSpecialty = new MedicalSpecialty();
        MedicalSpecialty medicalSpecialty1 = new MedicalSpecialty();
        medicalSpecialties.add(medicalSpecialty);
        medicalSpecialties.add(medicalSpecialty1);

        when(prescriptionRestrictionRepository.countAllByMedicalSpecialtyIn(medicalSpecialties)).thenReturn(0);

        prescriptionRestrictionService.validateSpecialties(medicalSpecialties);

        verify(prescriptionRestrictionRepository, times(1)).countAllByMedicalSpecialtyIn(medicalSpecialties);
    }

}
