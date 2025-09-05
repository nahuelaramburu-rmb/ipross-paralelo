package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.projection.AuditTrayProjection;
import com.capacidad.validationapi.module.audittray.repository.AuditTrayRepository;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditTrayServiceImplTest {

    private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    @Mock
    private AuditTrayRepository auditTrayRepository;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private NomenclatorService nomenclatorService;
    @Mock
    private RegionService regionService;
    @Mock
    private AuditorService auditorService;
    @Spy
    @InjectMocks
    private AuditTrayServiceImpl auditTrayService;

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenHasCityAndRegion() {
        AuditTray auditTray = new AuditTray();

        Region region = new Region();
        region.setId(1L);

        City city = new City();
        city.setId(1L);

        auditTray.setRegion(region);
        auditTray.setCity(city);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditTrayService.validate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.regionOrCityRequired");
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenDoesNotHaveCityOrRegion() {
        AuditTray auditTray = new AuditTray();
        auditTray.setCity(null);
        auditTray.setRegion(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditTrayService.validate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.regionOrCityRequired");
    }

    @Test
    public void testValidateThrowsObjectAlreadyExistsExceptionWhenNomenclatorInUseByRegion() throws ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();
        auditTray.getNomenclators().add(nomenclator2);
        auditTray.getNomenclators().add(nomenclator1);

        Region region = new Region();
        region.setId(1L);
        region.getCities().add(new City());

        auditTray.setRegion(region);

        when(regionService.findById(1L)).thenReturn(region);
        when(auditTrayRepository.existsByCityOrRegion(auditTray.getNomenclators(), region.getCities()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> auditTrayService.validate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.nomenclatorsAlreadyInUseInRegion");
    }

    @Test
    public void testValidateDoesNotThrowObjectAlreadyExistsExceptionWhenNomenclatorNotInUseByRegion() throws ObjectNotValidException, ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();
        auditTray.getNomenclators().add(nomenclator2);
        auditTray.getNomenclators().add(nomenclator1);

        Region region = new Region();
        region.setId(1L);
        region.getCities().add(new City());

        auditTray.setRegion(region);

        when(regionService.findById(1L)).thenReturn(region);
        when(auditTrayRepository.existsByCityOrRegion(auditTray.getNomenclators(), region.getCities()))
                .thenReturn(false);

        auditTrayService.validate(auditTray);
    }

    @Test
    public void testValidateThrowsObjectAlreadyExistsExceptionWhenNomenclatorInUseByCity() {
        AuditTray auditTray = new AuditTray();

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();
        auditTray.getNomenclators().add(nomenclator2);
        auditTray.getNomenclators().add(nomenclator1);

        City city = new City();
        city.setId(1L);

        auditTray.setCity(city);

        when(auditTrayRepository.existsByCityOrRegion(auditTray.getNomenclators(), Collections.singleton(city)))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> auditTrayService.validate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.nomenclatorsAlreadyInUseInCity");
    }


    @Test
    public void testValidateDoNotThrowsObjectAlreadyExistsExceptionWhenNomenclatorNotInUseByCity() throws ObjectNotValidException, ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();
        auditTray.getNomenclators().add(nomenclator2);
        auditTray.getNomenclators().add(nomenclator1);

        City city = new City();
        city.setId(1L);

        auditTray.setCity(city);

        when(auditTrayRepository.existsByCityOrRegion(auditTray.getNomenclators(), Collections.singleton(city)))
                .thenReturn(false);

        auditTrayService.validate(auditTray);
    }

    @Test
    public void testValidateUpdateThrowsObjectNotValidExceptionWhenHasCityAndRegion() {
        AuditTray auditTray = new AuditTray();

        City city = new City();
        Region region = new Region();

        auditTray.setCity(city);
        auditTray.setRegion(region);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditTrayService.validateUpdate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.regionOrCityRequired");
    }

    @Test
    public void testValidateUpdateThrowsObjectNotValidExceptionWhenDoesNotHaveCityOrRegion() {
        AuditTray auditTray = new AuditTray();
        auditTray.setCity(null);
        auditTray.setRegion(null);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> auditTrayService.validateUpdate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.regionOrCityRequired");
    }

    @Test
    public void testValidateUpdateThrowsObjectAlreadyExistExceptionWhenNomenclatorInUseByRegion() throws ObjectNotFoundException {

        Region region = new Region();
        region.setId(1L);
        region.getCities().add(new City());

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();

        AuditTray auditTray = new AuditTray();
        auditTray.setId(1L);
        auditTray.setRegion(region);
        auditTray.setCity(null);
        auditTray.getNomenclators().add(nomenclator1);
        auditTray.getNomenclators().add(nomenclator2);

        when(regionService.findById(1L)).thenReturn(region);
        when(auditTrayRepository.existsByCityOrRegionAndIdIsNot(auditTray.getNomenclators(), region.getCities(), auditTray.getId()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> auditTrayService.validateUpdate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.nomenclatorsAlreadyInUseInRegion");
    }

    @Test
    public void testValidateUpdateDoesNotThrowObjectAlreadyExistExceptionWhenNomenclatorInUseByRegion() throws ObjectNotValidException, ObjectNotFoundException {

        Region region = new Region();
        region.setId(1L);
        region.getCities().add(new City());

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();

        AuditTray auditTray = new AuditTray();
        auditTray.setId(1L);
        auditTray.setRegion(region);
        auditTray.setCity(null);
        auditTray.getNomenclators().add(nomenclator1);
        auditTray.getNomenclators().add(nomenclator2);

        when(regionService.findById(1L)).thenReturn(region);
        when(auditTrayRepository.existsByCityOrRegionAndIdIsNot(auditTray.getNomenclators(), region.getCities(), auditTray.getId()))
                .thenReturn(false);

        auditTrayService.validateUpdate(auditTray);
    }

    @Test
    public void testValidateUpdateThrowsObjectAlreadyExistExceptionWhenNomenclatorInUseByCity() {

        City city = new City();
        city.setId(1L);

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();

        AuditTray auditTray = new AuditTray();
        auditTray.setId(1L);
        auditTray.setCity(city);
        auditTray.setRegion(null);
        auditTray.getNomenclators().add(nomenclator1);
        auditTray.getNomenclators().add(nomenclator2);

        when(auditTrayRepository.existsByCityOrRegionAndIdIsNot(auditTray.getNomenclators(), Collections.singleton(city), auditTray.getId()))
                .thenReturn(true);

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> auditTrayService.validateUpdate(auditTray));

        assertThat(exception.getMessage()).isEqualTo("auditTray.nomenclatorsAlreadyInUseInCity");

    }

    @Test
    public void testValidateUpdateDoesNotThrowObjectAlreadyExistExceptionWhenNomenclatorNotInUseByCity() throws ObjectNotFoundException, ObjectNotValidException {

        City city = new City();
        city.setId(1L);

        Nomenclator nomenclator1 = new Nomenclator();
        Nomenclator nomenclator2 = new Nomenclator();

        AuditTray auditTray = new AuditTray();
        auditTray.setId(1L);
        auditTray.setCity(city);
        auditTray.setRegion(null);
        auditTray.getNomenclators().add(nomenclator1);
        auditTray.getNomenclators().add(nomenclator2);

        when(auditTrayRepository.existsByCityOrRegionAndIdIsNot(auditTray.getNomenclators(), Collections.singleton(city), auditTray.getId()))
                .thenReturn(false);

        auditTrayService.validateUpdate(auditTray);

    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testAddNomenclatorThrowsObjectAlreadyExistExceptionWhenNomenclatorAlreadyExist() throws ObjectNotValidException, ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);
        auditTray.getNomenclators().add(nomenclator);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(nomenclatorService.findById(1L)).thenReturn(nomenclator);

        auditTrayService.addNomenclator(1L, 1L);
    }

    @Test
    public void testAddNomenclatorExecutesSuccessfullyAndReturnsNomenclatorProjection() throws ObjectNotValidException, ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();
        Nomenclator nomenclator = new Nomenclator();
        Nomenclator nomenclatorToAdd = new Nomenclator();
        nomenclatorToAdd.setNomenclatorCode("12345");
        auditTray.getNomenclators().add(nomenclator);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(nomenclatorService.findById(1L)).thenReturn(nomenclatorToAdd);
        when(auditTrayRepository.save(auditTray)).thenReturn(auditTray);
        when(auditTrayService.getProjectionFactory()).thenReturn(projectionFactory);

        AuditTrayProjection.Extended result = auditTrayService.addNomenclator(1L, 1L);

        assertThat(result.getNomenclators().size()).isEqualTo(2);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRemoveNomenclatorThrowsObjectNotFoundExceptionWhenNomenclatorDoesNotExist() throws ObjectNotValidException, ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();

        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setId(1L);

        Nomenclator nomenclator2 = new Nomenclator();
        nomenclator2.setId(2L);

        auditTray.getNomenclators().add(nomenclator);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(nomenclatorService.findById(1L)).thenReturn(nomenclator2);

        auditTrayService.removeNomenclator(1L, 1L);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testRemoveNomenclatorThrowsObjectNotValidExceptionWhenNomenclatorIsTheLastRemaining() throws ObjectNotValidException, ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();
        Nomenclator nomenclator = new Nomenclator();
        auditTray.getNomenclators().add(nomenclator);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(nomenclatorService.findById(1L)).thenReturn(nomenclator);

        auditTrayService.removeNomenclator(1L, 1L);
    }

    @Test
    public void testRemoveNomenclatorExecutesSuccessfullyAndReturnsNomenclatorProjection() throws ObjectNotValidException, ObjectNotFoundException {
        AuditTray auditTray = new AuditTray();
        Nomenclator nomenclator = new Nomenclator();
        Nomenclator nomenclatorToRemove = new Nomenclator();
        nomenclatorToRemove.setNomenclatorCode("12345");
        auditTray.getNomenclators().add(nomenclator);
        auditTray.getNomenclators().add(nomenclatorToRemove);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(nomenclatorService.findById(1L)).thenReturn(nomenclatorToRemove);
        when(auditTrayRepository.save(auditTray)).thenReturn(auditTray);
        when(auditTrayService.getProjectionFactory()).thenReturn(projectionFactory);

        AuditTrayProjection.Extended result = auditTrayService.removeNomenclator(1L, 1L);

        assertThat(result.getNomenclators().size()).isEqualTo(1);
    }

    @Test(expected = ObjectAlreadyExistsException.class)
    public void testAddAuditorThrowsObjectAlreadyExistWhenAuditorIsAlreadyAssignedToTray() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        AuditTray auditTray = new AuditTray();
        Auditor auditor = new Auditor();
        auditTray.getAuditors().add(auditor);

        AuditorDTO auditorDTO = new AuditorDTO();
        UUID sub = UUID.randomUUID();
        auditor.setSub(sub);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(auditTrayService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(auditorDTO, Auditor.class)).thenReturn(auditor);
        when(auditorService.findOptionallyBySub(sub)).thenReturn(Optional.of(auditor));

        auditTrayService.addAuditor(1L, auditorDTO);
    }

    @Test
    public void testAddAuditorExecutesSuccessFullyAndReturnsAuditorProjection() throws ObjectNotFoundException, ObjectAlreadyExistsException {
        AuditTray auditTray = new AuditTray();
        Auditor auditorToAdd = new Auditor();

        AuditorDTO auditorDTO = new AuditorDTO();
        UUID sub = UUID.randomUUID();
        auditorToAdd.setSub(sub);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(auditTrayService.getObjectMapper()).thenReturn(objectMapper);
        when(objectMapper.convertValue(auditorDTO, Auditor.class)).thenReturn(auditorToAdd);
        when(auditorService.findOptionallyBySub(sub)).thenReturn(Optional.of(auditorToAdd));
        when(auditTrayService.getProjectionFactory()).thenReturn(projectionFactory);
        when(auditTrayRepository.save(auditTray)).thenReturn(auditTray);

        AuditTrayProjection.Extended result = auditTrayService.addAuditor(1L, auditorDTO);

        assertThat(result.getAuditors().size()).isEqualTo(1);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testRemoveAuditorThrowsObjectNotFoundExceptionWhenAuditorIsNotAssignedToAuditTray() throws ObjectNotFoundException, ObjectNotValidException {
        AuditTray auditTray = new AuditTray();
        Auditor auditorToRemove = new Auditor();
        auditorToRemove.setSub(UUID.randomUUID());

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(auditorService.findById(1L)).thenReturn(auditorToRemove);

        auditTrayService.removeAuditor(1L, 1L);
    }

    @Test(expected = ObjectNotValidException.class)
    public void testRemoveAuditorThrowsObjectNotValidExceptionWhenAuditorIsLastAssignedToAuditTray() throws ObjectNotFoundException, ObjectNotValidException {
        AuditTray auditTray = new AuditTray();
        Auditor auditorToRemove = new Auditor();
        auditTray.getAuditors().add(auditorToRemove);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(auditorService.findById(1L)).thenReturn(auditorToRemove);

        auditTrayService.removeAuditor(1L, 1L);
    }

    @Test
    public void testRemoveAuditorExecuteSuccessfullyWhenValidDataProvided() throws ObjectNotFoundException, ObjectNotValidException {
        AuditTray auditTray = new AuditTray();
        Auditor auditorToRemove = new Auditor();
        auditTray.getAuditors().add(new Auditor());
        auditTray.getAuditors().add(auditorToRemove);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        when(auditorService.findById(1L)).thenReturn(auditorToRemove);
        when(auditTrayRepository.save(auditTray)).thenReturn(auditTray);

        AuditTrayProjection.Extended result = auditTrayService.removeAuditor(1L, 1L);

        assertThat(result.getAuditors().size()).isEqualTo(1);
    }

    @Test
    public void testBuildAuditTrayBroadcastTopicNameWithoutPrefixIsValid() {
        String activeProfile = "dev";
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenant(tenantId);
        when(applicationProperties.getActiveProfile()).thenReturn(activeProfile);
        UUID auditTrayResourceId = UUID.randomUUID();
        String result = auditTrayService.buildAuditTrayBroadcastTopicName(auditTrayResourceId.toString(), false);

        assertThat(result).isEqualTo(StringUtils.join(activeProfile, DOT, tenantId.toString(), DOT, "audit-trays", DOT, auditTrayResourceId.toString(), DOT, "broadcast"));
    }

    @Test
    public void testBuildAuditTrayBroadcastTopicNameWithPrefixIsValid() {
        String activeProfile = "dev";
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenant(tenantId);
        when(applicationProperties.getActiveProfile()).thenReturn(activeProfile);
        UUID auditTrayResourceId = UUID.randomUUID();
        String result = auditTrayService.buildAuditTrayBroadcastTopicName(auditTrayResourceId.toString(), true);

        assertThat(result).isEqualTo(StringUtils.join("/topic/", activeProfile, DOT, tenantId.toString(), DOT, "audit-trays", DOT, auditTrayResourceId.toString(), DOT, "broadcast"));
    }

    @Test
    public void testBuildAuditTrayOnlineQueueNameWithoutPrefixIsValid() {
        String activeProfile = "dev";
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenant(tenantId);
        when(applicationProperties.getActiveProfile()).thenReturn(activeProfile);
        UUID auditTrayResourceId = UUID.randomUUID();
        String result = auditTrayService.buildAuditTrayOnlineQueueName(auditTrayResourceId.toString(), false);

        assertThat(result).isEqualTo(StringUtils.join(activeProfile, DOT, tenantId.toString(), DOT, "audit-trays", DOT, auditTrayResourceId.toString(), DOT, "online"));
    }

    @Test
    public void testBuildAuditTrayOnlineQueueNameWithPrefixIsValid() {
        String activeProfile = "dev";
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenant(tenantId);
        when(applicationProperties.getActiveProfile()).thenReturn(activeProfile);
        UUID auditTrayResourceId = UUID.randomUUID();
        String result = auditTrayService.buildAuditTrayOnlineQueueName(auditTrayResourceId.toString(), true);

        assertThat(result).isEqualTo(StringUtils.join("/queue/", activeProfile, DOT, tenantId.toString(), DOT, "audit-trays", DOT, auditTrayResourceId.toString(), DOT, "online"));
    }

    @Test
    public void testBuildAuditorConnectionsTopicNameIsValid() {
        String activeProfile = "dev";
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenant(tenantId);
        when(applicationProperties.getActiveProfile()).thenReturn(activeProfile);
        String result = auditTrayService.buildAuditorConnectionsTopicName();

        assertThat(result).isEqualTo(StringUtils.join("/topic/", activeProfile, DOT, tenantId.toString(), DOT, "auditor.connections"));
    }

}
