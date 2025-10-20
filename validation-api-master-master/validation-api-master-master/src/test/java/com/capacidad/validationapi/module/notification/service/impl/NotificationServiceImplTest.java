package com.capacidad.validationapi.module.notification.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.notification.dto.BeneficiaryNotificationDTO;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    public void testSendNotificationToBeneficiaryFamilyGroupIncludingFamilyResourceIds() throws ObjectNotFoundException, ObjectNotValidException {
        TenantContext.setTenant(UUID.randomUUID());

        var beneficiaryNotificationDTO = new BeneficiaryNotificationDTO();
        beneficiaryNotificationDTO.setBeneficiaryCode("112233-445566");
        beneficiaryNotificationDTO.setNotifyFamilyGroup(true);
        beneficiaryNotificationDTO.setBody("body");
        beneficiaryNotificationDTO.setTitle("title");

        var beneficiary = new Beneficiary();
        beneficiary.setFamilyId(UUID.randomUUID());
        beneficiary.setResourceId(UUID.randomUUID());

        var beneficiaryRelative = new Beneficiary();
        beneficiaryRelative.setResourceId(UUID.randomUUID());

        var familySet = new HashSet<Beneficiary>();
        familySet.add(beneficiary);
        familySet.add(beneficiaryRelative);

        when(beneficiaryFinder.findBeneficiary(beneficiaryNotificationDTO.getBeneficiaryCode())).thenReturn(beneficiary);
        when(beneficiaryFinder.getFamily(beneficiary.getFamilyId())).thenReturn(familySet);

        Notification result = notificationService.sendNotificationToBeneficiaryFamilyGroup(beneficiaryNotificationDTO);

        verify(notificationPublisher, times(1)).publishToResourceId(any(), anyList());
        verify(beneficiaryFinder, times(1)).getFamily(beneficiary.getFamilyId());
        assertThat(result.getTitle()).isEqualTo(beneficiaryNotificationDTO.getTitle());
        assertThat(result.getBody()).isEqualTo(beneficiaryNotificationDTO.getBody());

        TenantContext.clearTenant();
    }

    @Test
    public void testSendNotificationToBeneficiaryFamilyGroupWithoutFamilyResourceIds() throws ObjectNotFoundException, ObjectNotValidException {
        TenantContext.setTenant(UUID.randomUUID());

        var beneficiaryNotificationDTO = new BeneficiaryNotificationDTO();
        beneficiaryNotificationDTO.setBeneficiaryCode("112233-445566");
        beneficiaryNotificationDTO.setNotifyFamilyGroup(false);
        beneficiaryNotificationDTO.setBody("body");
        beneficiaryNotificationDTO.setTitle("title");

        var beneficiary = new Beneficiary();
        beneficiary.setResourceId(UUID.randomUUID());

        when(beneficiaryFinder.findBeneficiary(beneficiaryNotificationDTO.getBeneficiaryCode())).thenReturn(beneficiary);

        Notification result = notificationService.sendNotificationToBeneficiaryFamilyGroup(beneficiaryNotificationDTO);

        verify(notificationPublisher, times(1)).publishToResourceId(any(), anyList());
        verify(beneficiaryFinder, never()).getFamily(beneficiary.getFamilyId());
        assertThat(result.getTitle()).isEqualTo(beneficiaryNotificationDTO.getTitle());
        assertThat(result.getBody()).isEqualTo(beneficiaryNotificationDTO.getBody());

        TenantContext.clearTenant();
    }

    @Test
    public void testSendNotificationToBeneficiaryFamilyGroupWithMissingExtraDataType() throws ObjectNotFoundException {
        TenantContext.setTenant(UUID.randomUUID());

        var beneficiaryNotificationDTO = new BeneficiaryNotificationDTO();
        beneficiaryNotificationDTO.setBeneficiaryCode("112233-445566");
        beneficiaryNotificationDTO.setNotifyFamilyGroup(false);
        beneficiaryNotificationDTO.setBody("body");
        beneficiaryNotificationDTO.setTitle("title");

        var extraData = new HashMap<String, Object>();
        extraData.put("key", "value");

        beneficiaryNotificationDTO.setExtraData(extraData);

        var beneficiary = new Beneficiary();
        beneficiary.setResourceId(UUID.randomUUID());

        when(beneficiaryFinder.findBeneficiary(beneficiaryNotificationDTO.getBeneficiaryCode())).thenReturn(beneficiary);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> notificationService.sendNotificationToBeneficiaryFamilyGroup(beneficiaryNotificationDTO));

        assertThat(exception.getMessage()).isEqualTo("notification.invalidType");

        TenantContext.clearTenant();
    }

    @Test
    public void testSendNotificationToBeneficiaryFamilyGroupWithEmptyExtraDataType() throws ObjectNotFoundException {
        TenantContext.setTenant(UUID.randomUUID());

        var beneficiaryNotificationDTO = new BeneficiaryNotificationDTO();
        beneficiaryNotificationDTO.setBeneficiaryCode("112233-445566");
        beneficiaryNotificationDTO.setNotifyFamilyGroup(false);
        beneficiaryNotificationDTO.setBody("body");
        beneficiaryNotificationDTO.setTitle("title");

        var extraData = new HashMap<String, Object>();
        extraData.put("type", "");

        beneficiaryNotificationDTO.setExtraData(extraData);

        var beneficiary = new Beneficiary();
        beneficiary.setResourceId(UUID.randomUUID());

        when(beneficiaryFinder.findBeneficiary(beneficiaryNotificationDTO.getBeneficiaryCode())).thenReturn(beneficiary);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> notificationService.sendNotificationToBeneficiaryFamilyGroup(beneficiaryNotificationDTO));

        assertThat(exception.getMessage()).isEqualTo("notification.invalidType");

        TenantContext.clearTenant();
    }

    @Test
    public void testSendNotificationToBeneficiaryFamilyGroupWithInvalidExtraDataType() throws ObjectNotFoundException {
        TenantContext.setTenant(UUID.randomUUID());

        var beneficiaryNotificationDTO = new BeneficiaryNotificationDTO();
        beneficiaryNotificationDTO.setBeneficiaryCode("112233-445566");
        beneficiaryNotificationDTO.setNotifyFamilyGroup(false);
        beneficiaryNotificationDTO.setBody("body");
        beneficiaryNotificationDTO.setTitle("title");

        var extraData = new HashMap<String, Object>();
        extraData.put("type", 1234);

        beneficiaryNotificationDTO.setExtraData(extraData);

        var beneficiary = new Beneficiary();
        beneficiary.setResourceId(UUID.randomUUID());

        when(beneficiaryFinder.findBeneficiary(beneficiaryNotificationDTO.getBeneficiaryCode())).thenReturn(beneficiary);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> notificationService.sendNotificationToBeneficiaryFamilyGroup(beneficiaryNotificationDTO));

        assertThat(exception.getMessage()).isEqualTo("notification.invalidType");

        TenantContext.clearTenant();
    }

    @Test
    public void testSendNotificationToBeneficiaryFamilyGroupWithValidExtraData() throws ObjectNotFoundException, ObjectNotValidException {
        TenantContext.setTenant(UUID.randomUUID());

        var beneficiaryNotificationDTO = new BeneficiaryNotificationDTO();
        beneficiaryNotificationDTO.setBeneficiaryCode("112233-445566");
        beneficiaryNotificationDTO.setNotifyFamilyGroup(false);
        beneficiaryNotificationDTO.setBody("body");
        beneficiaryNotificationDTO.setTitle("title");

        var extraData = new HashMap<String, Object>();
        extraData.put("type", "NEW_TYPE");
        extraData.put("key1", "value1");
        extraData.put("key2", "value2");

        beneficiaryNotificationDTO.setExtraData(extraData);

        var beneficiary = new Beneficiary();
        beneficiary.setResourceId(UUID.randomUUID());

        when(beneficiaryFinder.findBeneficiary(beneficiaryNotificationDTO.getBeneficiaryCode())).thenReturn(beneficiary);

        Notification result = notificationService.sendNotificationToBeneficiaryFamilyGroup(beneficiaryNotificationDTO);

        verify(notificationPublisher, times(1)).publishToResourceId(any(), anyList());
        verify(beneficiaryFinder, never()).getFamily(beneficiary.getFamilyId());
        assertThat(result.getTitle()).isEqualTo(beneficiaryNotificationDTO.getTitle());
        assertThat(result.getBody()).isEqualTo(beneficiaryNotificationDTO.getBody());
        assertThat(result.getExtraData().get("type")).isEqualTo(extraData.get("type"));
        assertThat(result.getExtraData().get("key1")).isEqualTo(extraData.get("key1"));
        assertThat(result.getExtraData().get("key2")).isEqualTo(extraData.get("key2"));

        TenantContext.clearTenant();
    }

}
