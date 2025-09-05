package com.capacidad.validationapi.prescription.galbop.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicine.model.Medicine;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPlan;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPlanEmbeddedId;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescription;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionEmbeddedId;
import com.capacidad.validationapi.prescription.galbop.repository.GalbopPlanRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GalbopPrescriptionValidatorImplTest {

    @Mock
    private GalbopPlanRepository galbopPlanRepository;

    @InjectMocks
    private GalbopPrescriptionValidatorImpl galbopPrescriptionValidator;

    @Test
    public void testValidateDoesNothingWhenEmptyPlanQuantityRules() throws ObjectNotFoundException, ObjectNotValidException {
        GalbopPrescription galbopPrescription = mock(GalbopPrescription.class);
        Prescription prescription = mock(Prescription.class);

        GalbopPrescriptionEmbeddedId embeddedId = new GalbopPrescriptionEmbeddedId();
        embeddedId.setPlanId(1L);

        GalbopPlan galbopPlan = new GalbopPlan();
        galbopPlan.setQuantityRules(null);

        when(galbopPrescription.getId()).thenReturn(embeddedId);
        when(galbopPlanRepository.findById(any(GalbopPlanEmbeddedId.class))).thenReturn(Optional.of(galbopPlan));

        galbopPrescriptionValidator.validate(galbopPrescription, prescription);

        verify(prescription, never()).getPrescriptionItems();
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidPrescriptionLineQuantityInDefaultSize() {
        GalbopPrescription galbopPrescription = new GalbopPrescription();
        Prescription prescription = new Prescription();

        GalbopPrescriptionEmbeddedId embeddedId = new GalbopPrescriptionEmbeddedId();
        embeddedId.setPlanId(1L);

        galbopPrescription.setId(embeddedId);

        GalbopPlan galbopPlan = new GalbopPlan();
        galbopPlan.setQuantityRules("invalid;T(1,2,2);T(3,1,1);T(6,5,5);invalid");

        PrescriptionItem invalidPrescriptionItem = new PrescriptionItem();
        invalidPrescriptionItem.setQuantity(2);
        Medicine invalidMedicine = new Medicine();
        invalidMedicine.setProduct("invalid medicine size 9");
        invalidMedicine.setSizeId(9L);
        invalidMedicine.setExchangeId(2L);
        invalidPrescriptionItem.setMedicine(invalidMedicine);

        prescription.getPrescriptionItems().add(invalidPrescriptionItem);

        when(galbopPlanRepository.findById(any(GalbopPlanEmbeddedId.class))).thenReturn(Optional.of(galbopPlan));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> galbopPrescriptionValidator.validate(galbopPrescription, prescription));

        assertThat(exception.getMessage()).isEqualTo("prescription.invalidMaxInLine");
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidPrescriptionLineQuantityNotDefaultSize() {
        GalbopPrescription galbopPrescription = new GalbopPrescription();
        Prescription prescription = new Prescription();

        GalbopPrescriptionEmbeddedId embeddedId = new GalbopPrescriptionEmbeddedId();
        embeddedId.setPlanId(1L);

        galbopPrescription.setId(embeddedId);

        GalbopPlan galbopPlan = new GalbopPlan();
        galbopPlan.setQuantityRules("invalid;T(1,2,2);T(3,1,1);T(6,5,5);invalid");

        PrescriptionItem invalidPrescriptionItem = new PrescriptionItem();
        invalidPrescriptionItem.setQuantity(2);
        Medicine invalidMedicine = new Medicine();
        invalidMedicine.setProduct("invalid medicine size 3");
        invalidMedicine.setSizeId(3L);
        invalidMedicine.setExchangeId(2L);
        invalidPrescriptionItem.setMedicine(invalidMedicine);

        prescription.getPrescriptionItems().add(invalidPrescriptionItem);

        when(galbopPlanRepository.findById(any(GalbopPlanEmbeddedId.class))).thenReturn(Optional.of(galbopPlan));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> galbopPrescriptionValidator.validate(galbopPrescription, prescription));

        assertThat(exception.getMessage()).isEqualTo("prescription.invalidMaxInLine");
    }

    @Test
    public void testValidateThrowsExceptionWhenInvalidPrescriptionTotalQuantity() {
        GalbopPrescription galbopPrescription = new GalbopPrescription();
        Prescription prescription = new Prescription();

        GalbopPrescriptionEmbeddedId embeddedId = new GalbopPrescriptionEmbeddedId();
        embeddedId.setPlanId(1L);

        galbopPrescription.setId(embeddedId);

        GalbopPlan galbopPlan = new GalbopPlan();
        galbopPlan.setQuantityRules("invalid;T(1,2,2);T(3,1,1);T(6,5,5);invalid");

        PrescriptionItem prescriptionItem = new PrescriptionItem();
        prescriptionItem.setQuantity(1);
        Medicine medicine = new Medicine();
        medicine.setProduct("medicine 1");
        medicine.setSizeId(9L);
        medicine.setExchangeId(1L);
        prescriptionItem.setMedicine(medicine);

        PrescriptionItem prescriptionItem2 = new PrescriptionItem();
        prescriptionItem2.setQuantity(1);
        Medicine medicine2 = new Medicine();
        medicine2.setProduct("medicine 2");
        medicine2.setSizeId(3L);
        medicine2.setExchangeId(2L);
        prescriptionItem2.setMedicine(medicine2);

        prescription.getPrescriptionItems().add(prescriptionItem);
        prescription.getPrescriptionItems().add(prescriptionItem2);

        when(galbopPlanRepository.findById(any(GalbopPlanEmbeddedId.class))).thenReturn(Optional.of(galbopPlan));

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> galbopPrescriptionValidator.validate(galbopPrescription, prescription));

        assertThat(exception.getMessage()).isEqualTo("prescription.invalidMaxInPrescription");
    }

    @Test
    public void testValidateDoNotThrowsExceptionWhenValidPrescriptionAndLineQuantity() throws ObjectNotFoundException, ObjectNotValidException {
        GalbopPrescription galbopPrescription = new GalbopPrescription();
        Prescription prescription = new Prescription();

        GalbopPrescriptionEmbeddedId embeddedId = new GalbopPrescriptionEmbeddedId();
        embeddedId.setPlanId(1L);

        galbopPrescription.setId(embeddedId);

        GalbopPlan galbopPlan = new GalbopPlan();
        galbopPlan.setQuantityRules("invalid;T(1,2,2);T(3,1,1);T(6,5,5);invalid");

        PrescriptionItem prescriptionItem = new PrescriptionItem();
        prescriptionItem.setQuantity(1);
        Medicine medicine = mock(Medicine.class);
        when(medicine.getProduct()).thenReturn("medicine 1");
        when(medicine.getSizeId()).thenReturn(3L);
        prescriptionItem.setMedicine(medicine);

        PrescriptionItem prescriptionItem2 = new PrescriptionItem();
        prescriptionItem2.setQuantity(1);
        Medicine medicine2 = mock(Medicine.class);
        when(medicine2.getProduct()).thenReturn("medicine 2");
        when(medicine2.getSizeId()).thenReturn(1L);
        prescriptionItem2.setMedicine(medicine2);

        prescription.getPrescriptionItems().add(prescriptionItem);
        prescription.getPrescriptionItems().add(prescriptionItem2);

        when(galbopPlanRepository.findById(any(GalbopPlanEmbeddedId.class))).thenReturn(Optional.of(galbopPlan));

        galbopPrescriptionValidator.validate(galbopPrescription, prescription);

        verify(medicine, times(1)).getProduct();
        verify(medicine2, times(1)).getProduct();
    }

}
