package com.capacidad.validationapi.module.medicalcoverage.service.impl;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.medicalcoverage.model.ChargeType;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverageItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.catchThrowable;

@RunWith(MockitoJUnitRunner.class)
public class MedicalCoverageItemServiceImplTest {

    @InjectMocks
    private MedicalCoverageItemServiceImpl medicalCoverageItemService;

    @Test
    public void testValidateFailsWhenChangeValueIsNullAndChargeTypeIsNot() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(null);
        medicalCoverageItem.setChargeType(new ChargeType());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageItemService.validate(medicalCoverageItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverageItem.invalidChargeTypeOrValue");
    }

    @Test
    public void testValidateFailsWhenChangeTypeIsNullAndChargeValueIsNot() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageItemService.validate(medicalCoverageItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverageItem.invalidChargeTypeOrValue");
    }

    @Test
    public void testValidateFailsWhenFixedMaxDaysIsNullAndFixedMaxQuantityIsNot() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());
        medicalCoverageItem.setFixedMaxDays(null);
        medicalCoverageItem.setFixedMaxQuantity(5);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageItemService.validate(medicalCoverageItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverageItem.invalidFixedMaxDaysOrQuantity");
    }

    @Test
    public void testValidateFailsWhenFixedMaxQuantityIsNullAndFixedMaxDaysIsNot() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageItemService.validate(medicalCoverageItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverageItem.invalidFixedMaxDaysOrQuantity");
    }

    @Test
    public void testValidateFailsWhenAgeFromIsNullAndAgeToIsNot() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(5);
        medicalCoverageItem.setAgeFrom(null);
        medicalCoverageItem.setAgeTo(50);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageItemService.validate(medicalCoverageItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverageItem.invalidAgeFromOrAgeTo");
    }

    @Test
    public void testValidateFailsWhenAgeToIsNullAndAgeFromIsNot() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(5);
        medicalCoverageItem.setAgeFrom(10);
        medicalCoverageItem.setAgeTo(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageItemService.validate(medicalCoverageItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverageItem.invalidAgeFromOrAgeTo");
    }

    @Test
    public void testValidateFailsWhenAgeFromIsGreaterThanAgeTo() {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(5);
        medicalCoverageItem.setAgeFrom(50);
        medicalCoverageItem.setAgeTo(10);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> medicalCoverageItemService.validate(medicalCoverageItem));

        assertThat(exception.getMessage()).isEqualTo("medicalCoverageItem.invalidAgeTo");
    }

    @Test
    public void testValidateDoNotFailsWhenAllFieldsAreValid() throws ObjectNotValidException {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(5);
        medicalCoverageItem.setAgeFrom(10);
        medicalCoverageItem.setAgeTo(50);

        medicalCoverageItemService.validate(medicalCoverageItem);
    }

    @Test
    public void testValidateDoNotFailsWhenAllFieldsAreValidNoAge() throws ObjectNotValidException {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());
        medicalCoverageItem.setFixedMaxDays(1);
        medicalCoverageItem.setFixedMaxQuantity(5);

        medicalCoverageItemService.validate(medicalCoverageItem);
    }

    @Test
    public void testValidateDoNotFailsWhenAllFieldsAreValidNoFixedQuantity() throws ObjectNotValidException {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();
        medicalCoverageItem.setChargeValue(new BigDecimal(1234));
        medicalCoverageItem.setChargeType(new ChargeType());

        medicalCoverageItemService.validate(medicalCoverageItem);
    }

    @Test
    public void testValidateDoNotFailsWhenAllFieldsAreValidNoCharges() throws ObjectNotValidException {
        MedicalCoverageItem medicalCoverageItem = new MedicalCoverageItem();

        medicalCoverageItemService.validate(medicalCoverageItem);
    }

}
