package com.capacidad.validationapi.module.premedicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorService;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PrePopulatedPreMedicalAuthorizationDTO;
import com.capacidad.validationapi.module.premedicalauthorization.dto.PrePopulatedPreMedicalAuthorizationItemDTO;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorization;
import com.capacidad.validationapi.module.premedicalauthorization.model.PreMedicalAuthorizationItem;
import com.capacidad.validationapi.module.properties.model.Properties;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hashids.Hashids;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.PRE_MEDICAL_AUTHORIZATION_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PreMedicalAuthorizationBuilderImplTest {

    @Mock
    private BeneficiaryFinder beneficiaryFinder;

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private NomenclatorService nomenclatorService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PropertiesService propertiesService;

    @Mock
    private Hashids hashids;

    @Mock
    private Utils utils;

    @Spy
    @InjectMocks
    private PreMedicalAuthorizationBuilderImpl preMedicalAuthorizationBuilder;

    @Test
    public void testBuildIsSuccessfulWhenPreMedicalAuthorizationDTO() throws ObjectNotFoundException {
        PreMedicalAuthorizationDTO dto = new PreMedicalAuthorizationDTO();
        Properties properties = new Properties();
        properties.setPreAuthorizationMaxDays(30);
        String code = "ABC123";

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        when(objectMapper.convertValue(dto, PreMedicalAuthorization.class)).thenReturn(new PreMedicalAuthorization());
        when(propertiesService.getProperties()).thenReturn(properties);
        when(hashids.encode(any())).thenReturn(code);
        when(utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId())).thenReturn(active);

        PreMedicalAuthorization result = preMedicalAuthorizationBuilder.build(dto);

        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getStatus()).isEqualTo(active);
        assertThat(result.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(properties.getPreAuthorizationMaxDays()));
    }

    @Test
    public void testBuildIsSuccessfulWhenPrePopulatedPreMedicalAuthorizationDTOAndEmptyPetitioner() throws ObjectNotFoundException {
        PrePopulatedPreMedicalAuthorizationDTO dto = new PrePopulatedPreMedicalAuthorizationDTO();
        dto.setPetitionerIdNumber(null);
        dto.setBeneficiaryCode("invalid");

        PrePopulatedPreMedicalAuthorizationItemDTO itemDTO = new PrePopulatedPreMedicalAuthorizationItemDTO();
        itemDTO.setNomenclatorCode("420101");
        itemDTO.setChargeUnitPrice(new BigDecimal(100));
        itemDTO.setQuantity(1);
        Set<PrePopulatedPreMedicalAuthorizationItemDTO> itemDTOSet = Collections.singleton(itemDTO);

        dto.setPreMedicalAuthorizationItems(itemDTOSet);

        Properties properties = new Properties();
        properties.setPreAuthorizationMaxDays(30);
        String code = "ABC123";

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        when(propertiesService.getProperties()).thenReturn(properties);
        when(hashids.encode(any())).thenReturn(code);
        when(beneficiaryFinder.findBeneficiary(dto.getBeneficiaryCode())).thenReturn(new Beneficiary());
        when(nomenclatorService.findByNomenclatorCode(itemDTO.getNomenclatorCode())).thenReturn(new Nomenclator());
        when(utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId())).thenReturn(active);

        PreMedicalAuthorization result = preMedicalAuthorizationBuilder.build(dto);

        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(properties.getPreAuthorizationMaxDays()));
        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getPetitioner()).isNull();
        assertThat(result.getStatus()).isEqualTo(active);
        PreMedicalAuthorizationItem item = result.getPreMedicalAuthorizationItems().iterator().next();
        assertThat(item.getNomenclator()).isNotNull();
        assertThat(item.getChargeUnitPrice()).isEqualTo(itemDTO.getChargeUnitPrice());
        assertThat(item.getQuantity()).isEqualTo(itemDTO.getQuantity());
        assertThat(item.getRemaining()).isEqualTo(item.getQuantity());
    }

    @Test
    public void testBuildIsSuccessfulWhenPrePopulatedPreMedicalAuthorizationDTOAndValidPetitioner() throws ObjectNotFoundException {
        PrePopulatedPreMedicalAuthorizationDTO dto = new PrePopulatedPreMedicalAuthorizationDTO();
        dto.setPetitionerIdNumber(123456L);
        dto.setBeneficiaryCode("invalid");

        PrePopulatedPreMedicalAuthorizationItemDTO itemDTO = new PrePopulatedPreMedicalAuthorizationItemDTO();
        itemDTO.setNomenclatorCode("420101");
        itemDTO.setChargeUnitPrice(new BigDecimal(100));
        itemDTO.setQuantity(1);
        Set<PrePopulatedPreMedicalAuthorizationItemDTO> itemDTOSet = Collections.singleton(itemDTO);

        dto.setPreMedicalAuthorizationItems(itemDTOSet);

        Properties properties = new Properties();
        properties.setPreAuthorizationMaxDays(30);
        String code = "ABC123";

        Status active = new Status();
        active.setId(PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId());

        when(propertiesService.getProperties()).thenReturn(properties);
        when(hashids.encode(any())).thenReturn(code);
        when(beneficiaryFinder.findBeneficiary(dto.getBeneficiaryCode())).thenReturn(new Beneficiary());
        when(nomenclatorService.findByNomenclatorCode(itemDTO.getNomenclatorCode())).thenReturn(new Nomenclator());
        when(practitionerService.findOptionallyByIdTypeAndIdNumber(any(), anyLong())).thenReturn(Optional.of(new Practitioner()));
        when(utils.getGenericsEntityReference(Status.class, PRE_MEDICAL_AUTHORIZATION_ACTIVE.getId())).thenReturn(active);

        PreMedicalAuthorization result = preMedicalAuthorizationBuilder.build(dto);

        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(properties.getPreAuthorizationMaxDays()));
        assertThat(result.getBeneficiary()).isNotNull();
        assertThat(result.getPetitioner()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(active);
        PreMedicalAuthorizationItem item = result.getPreMedicalAuthorizationItems().iterator().next();
        assertThat(item.getNomenclator()).isNotNull();
        assertThat(item.getChargeUnitPrice()).isEqualTo(itemDTO.getChargeUnitPrice());
        assertThat(item.getQuantity()).isEqualTo(itemDTO.getQuantity());
        assertThat(item.getRemaining()).isEqualTo(item.getQuantity());
    }

}
