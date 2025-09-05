package com.capacidad.validationapi.module.base.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.audittray.dto.AuditTrayDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.projection.AuditTrayProjection;
import com.capacidad.validationapi.module.audittray.repository.AuditTrayRepository;
import com.capacidad.validationapi.module.audittray.service.impl.AuditTrayServiceImpl;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.model.AuditLog;
import com.capacidad.validationapi.module.base.projection.AuditLogProjection;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryUpdateDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryAuditLogProjection;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.repository.BeneficiaryRepository;
import com.capacidad.validationapi.module.beneficiary.service.impl.BeneficiaryServiceImpl;
import com.capacidad.validationapi.module.company.model.Company;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.nomenclator.dto.NomenclatorDTO;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.repository.NomenclatorRepository;
import com.capacidad.validationapi.module.nomenclator.service.impl.NomenclatorServiceImpl;
import com.capacidad.validationapi.module.person.model.RelationshipType;
import com.capacidad.validationapi.module.person.reference.RelationshipTypeReference;
import com.capacidad.validationapi.module.practitioner.model.MedicalSpecialty;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.projection.PractitionerProjection;
import com.capacidad.validationapi.module.practitioner.repository.PractitionerRepository;
import com.capacidad.validationapi.module.practitioner.service.impl.PractitionerServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.EntityModel;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BaseServiceImplTest {

    private final ObjectMapper realObjectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Utils utils;

    @Mock
    private AuditReader auditReader;

    @Mock
    private NomenclatorRepository nomenclatorRepository;

    @Mock
    private AuditTrayRepository auditTrayRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private PractitionerRepository practitionerRepository;

    @Mock
    private SmartValidator validator;

    @Spy
    @InjectMocks
    private AuditTrayServiceImpl auditTrayService;

    @Spy
    @InjectMocks
    private NomenclatorServiceImpl nomenclatorService;

    @Spy
    @InjectMocks
    private PractitionerServiceImpl practitionerService;

    @Spy
    @InjectMocks
    private BeneficiaryServiceImpl beneficiaryService;


    @Test
    public void testCreateReturnsEntityWhenValidDTO() throws ObjectNotValidException, ObjectNotFoundException {
        doReturn(objectMapper).when(nomenclatorService).getObjectMapper();

        NomenclatorDTO nomenclatorDTO = new NomenclatorDTO();

        Nomenclator nomenclator = new Nomenclator();

        doNothing().when(nomenclatorService).validate(nomenclator);
        when(objectMapper.convertValue(nomenclatorDTO, Nomenclator.class)).thenReturn(nomenclator);
        when(nomenclatorRepository.save(nomenclator)).thenReturn(nomenclator);

        Nomenclator result = nomenclatorService.create(nomenclatorDTO);

        assertThat(result).isEqualTo(nomenclator);
        verify(nomenclatorRepository, times(1)).save(nomenclator);
    }

    @Test
    public void testPartialUpdateThrowsObjectNotValidWhenFieldIsNotEditable() throws ObjectNotFoundException {
        doReturn(realObjectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();

        Beneficiary beneficiary = new Beneficiary();

        var updateValues = new HashMap<String, Object>();
        String immutableField = "resourceId";
        updateValues.put(immutableField, UUID.randomUUID());

        doReturn(beneficiary).when(beneficiaryService).findById(1L);

        RuntimeException exception = (RuntimeException) catchThrowable(() -> beneficiaryService.update(updateValues, 1L));

        assertThat(exception).hasMessageContaining("base.invalidField");
    }

    @Test
    public void testPartialUpdateThrowsObjectNotValidWhenValueIsNotOfValidType() throws ObjectNotFoundException, IOException {
        doReturn(objectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setName("name1");

        var updateValues = new HashMap<String, Object>();
        String fieldName = "name";
        updateValues.put(fieldName, new Object());

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        when(objectMapper.convertValue(updateValues, BeneficiaryUpdateDTO.class)).thenReturn(new BeneficiaryUpdateDTO());
        byte[] byteArr = new byte[0];
        when(objectMapper.writeValueAsBytes(any())).thenReturn(byteArr);
        when(objectMapper.readValue(byteArr, String.class)).thenThrow(new IOException(""));
        doNothing().when(validator).validate(any(), any(BindingResult.class));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> beneficiaryService.update(updateValues, 1L));

        assertThat(exception).hasMessageContaining("base.invalidFieldValue");
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsNotAnEmptyString() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();

        Beneficiary beneficiary = new Beneficiary();

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.HOLDER.getId());
        beneficiary.setRelationshipType(relationshipType);

        beneficiary.setName("name1");

        var updateValues = new HashMap<String, Object>();
        String fieldName = "name";
        updateValues.put(fieldName, "newName");

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doNothing().when(beneficiaryService).validateUpdate(beneficiary);
        doNothing().when(validator).validate(any(), any(BindingResult.class));
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);

        EntityModel<BeneficiaryProjection> resource = beneficiaryService.update(updateValues, 1L);

        assertThat(resource.getContent().getName()).isNotEmpty();
        assertThat(resource.getContent().getName()).isEqualTo("newName");
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsANumber() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();

        Beneficiary beneficiary = new Beneficiary();

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.HOLDER.getId());
        beneficiary.setRelationshipType(relationshipType);

        beneficiary.setIdNumber(12345L);

        var updateValues = new HashMap<String, Object>();
        String fieldName = "idNumber";
        updateValues.put(fieldName, 54321L);

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doNothing().when(beneficiaryService).validateUpdate(beneficiary);
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);

        EntityModel<BeneficiaryProjection> resource = beneficiaryService.update(updateValues, 1L);

        assertThat(resource.getContent().getIdNumber()).isNotNull();
        assertThat(resource.getContent().getIdNumber()).isEqualTo(54321L);
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsAReference() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();
        doReturn(utils).when(beneficiaryService).getUtils();

        Beneficiary beneficiary = new Beneficiary();

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.HOLDER.getId());
        beneficiary.setRelationshipType(relationshipType);

        beneficiary.setIdNumber(12345L);

        var updateValues = new HashMap<String, Object>();
        String fieldName = "company";
        IdDTO<Long> companyDTO = new IdDTO<>();
        companyDTO.setId(2L);
        updateValues.put(fieldName, companyDTO);

        Company companyReference = new Company();
        companyReference.setId(companyDTO.getId());

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doNothing().when(beneficiaryService).validateUpdate(beneficiary);
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
        when(utils.getGenericsEntityReference(Company.class, 2L)).thenReturn(companyReference);

        EntityModel<BeneficiaryProjection> resource = beneficiaryService.update(updateValues, 1L);

        assertThat(resource.getContent().getCompany()).isNotNull();
        assertThat(resource.getContent().getCompany().getId()).isEqualTo(companyDTO.getId());
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsANestedObjectWithSimpleNonExistentAttribute() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();

        Beneficiary beneficiary = new Beneficiary();

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.HOLDER.getId());
        beneficiary.setRelationshipType(relationshipType);

        beneficiary.setIdNumber(12345L);

        var updateValues = new HashMap<String, Object>();
        String fieldName = "address";
        var nestedValues = new HashMap<String, Object>();
        String districtValue = "newDistrict";
        nestedValues.put("district", districtValue);
        updateValues.put(fieldName, nestedValues);

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doNothing().when(beneficiaryService).validateUpdate(beneficiary);
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);

        EntityModel<BeneficiaryProjection> resource = beneficiaryService.update(updateValues, 1L);

        assertThat(resource.getContent()).isNotNull();
        assertThat(beneficiary.getAddress()).isNotNull();
        assertThat(beneficiary.getAddress().getDistrict()).isEqualTo(districtValue);
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsANestedObjectWithSimpleExistentAttribute() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setAddress(new Address());

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.HOLDER.getId());
        beneficiary.setRelationshipType(relationshipType);

        beneficiary.setIdNumber(12345L);

        var updateValues = new HashMap<String, Object>();
        String fieldName = "address";
        var nestedValues = new HashMap<String, Object>();
        String districtValue = "newDistrict";
        nestedValues.put("district", districtValue);
        updateValues.put(fieldName, nestedValues);

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doNothing().when(beneficiaryService).validateUpdate(beneficiary);
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);

        EntityModel<BeneficiaryProjection> resource = beneficiaryService.update(updateValues, 1L);

        assertThat(resource.getContent()).isNotNull();
        assertThat(beneficiary.getAddress()).isNotNull();
        assertThat(beneficiary.getAddress().getDistrict()).isEqualTo(districtValue);
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsANestedObjectWithNestedAttribute() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(beneficiaryService).getObjectMapper();
        doReturn(validator).when(beneficiaryService).getValidator();
        doReturn(utils).when(beneficiaryService).getUtils();

        Beneficiary beneficiary = new Beneficiary();

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setId(RelationshipTypeReference.HOLDER.getId());
        beneficiary.setRelationshipType(relationshipType);

        beneficiary.setIdNumber(12345L);

        City city = new City();
        city.setId(2L);

        var updateValues = new HashMap<String, Object>();
        var nestedValues1 = new HashMap<String, Object>();
        var nestedValues2 = new HashMap<String, Object>();

        nestedValues2.put("id", city.getId());
        nestedValues1.put("city", nestedValues2);
        updateValues.put("address", nestedValues1);

        doReturn(beneficiary).when(beneficiaryService).findById(1L);
        doNothing().when(beneficiaryService).validateUpdate(beneficiary);
        when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
        when(utils.getGenericsEntityReference(City.class, 2L)).thenReturn(city);

        EntityModel<BeneficiaryProjection> resource = beneficiaryService.update(updateValues, 1L);

        assertThat(resource.getContent()).isNotNull();
        assertThat(beneficiary.getAddress()).isNotNull();
        assertThat(beneficiary.getAddress().getCity().getId()).isEqualTo(city.getId());
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsACollectionOfReferences() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(practitionerService).getObjectMapper();
        doReturn(validator).when(practitionerService).getValidator();
        doReturn(utils).when(practitionerService).getUtils();

        Practitioner practitioner = new Practitioner();

        var updateValues = new HashMap<String, Object>();

        Set<IdDTO<Long>> medSpecialties = new HashSet<>();
        IdDTO<Long> medSpec1 = new IdDTO<>();
        medSpec1.setId(1L);

        IdDTO<Long> medSpec2 = new IdDTO<>();
        medSpec2.setId(2L);

        medSpecialties.add(medSpec1);
        medSpecialties.add(medSpec2);

        MedicalSpecialty medSpecReference1 = new MedicalSpecialty();
        medSpecReference1.setId(medSpec1.getId());
        MedicalSpecialty medSpecReference2 = new MedicalSpecialty();
        medSpecReference2.setId(medSpec2.getId());

        updateValues.put("medicalSpecialties", medSpecialties);

        doReturn(practitioner).when(practitionerService).findById(1L);
        doNothing().when(practitionerService).validateUpdate(practitioner);
        when(practitionerRepository.save(practitioner)).thenReturn(practitioner);
        when(utils.getGenericsEntityReference(MedicalSpecialty.class, medSpec1.getId())).thenReturn(medSpecReference1);
        when(utils.getGenericsEntityReference(MedicalSpecialty.class, medSpec2.getId())).thenReturn(medSpecReference2);

        EntityModel<PractitionerProjection> resource = practitionerService.update(updateValues, 1L);

        assertThat(resource.getContent()).isNotNull();
        assertThat(practitioner.getMedicalSpecialties()).isNotEmpty();
        assertThat(practitioner.getMedicalSpecialties().size()).isEqualTo(medSpecialties.size());
    }

    @Test
    public void testPartialUpdateExecuteSuccessfullyWhenValueIsACollectionOfObjects() throws ObjectNotFoundException, ObjectNotValidException {
        doReturn(realObjectMapper).when(auditTrayService).getObjectMapper();
        doReturn(validator).when(auditTrayService).getValidator();

        AuditTray auditTray = new AuditTray();

        var updateValues = new HashMap<String, Object>();

        Set<AuditorDTO> auditors = new HashSet<>();

        AuditorDTO auditorDTO1 = new AuditorDTO();
        auditorDTO1.setSub(UUID.randomUUID());
        auditorDTO1.setDisplayName("displayName1");
        auditorDTO1.setUsername("username1");

        AuditorDTO auditorDTO2 = new AuditorDTO();
        auditorDTO2.setSub(UUID.randomUUID());
        auditorDTO2.setDisplayName("displayName2");
        auditorDTO2.setUsername("username2");

        auditors.add(auditorDTO1);
        auditors.add(auditorDTO2);

        updateValues.put("auditors", auditors);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        doNothing().when(validator).validate(any(), any());
        doNothing().when(auditTrayService).validateUpdate(auditTray);
        when(auditTrayRepository.save(auditTray)).thenReturn(auditTray);

        EntityModel<AuditTrayProjection> resource = auditTrayService.update(updateValues, 1L);

        assertThat(resource.getContent()).isNotNull();
        assertThat(auditTray.getAuditors()).isNotEmpty();
        assertThat(auditTray.getAuditors().size()).isEqualTo(auditors.size());
    }

    @Test
    public void testPartialUpdateThrowsObjectNotValidExceptionWhenCollectionValuesAreInvalid() throws ObjectNotFoundException, IOException {
        doReturn(objectMapper).when(auditTrayService).getObjectMapper();
        doReturn(validator).when(auditTrayService).getValidator();

        AuditTray auditTray = new AuditTray();

        AuditTrayDTO auditTrayDTO = new AuditTrayDTO();

        var updateValues = new HashMap<String, Object>();

        Set<AuditorDTO> auditors = new HashSet<>();

        AuditorDTO auditorDTO1 = new AuditorDTO();
        auditorDTO1.setSub(UUID.randomUUID());
        auditorDTO1.setDisplayName("displayName1");
        auditorDTO1.setUsername("username1");

        auditors.add(auditorDTO1);

        auditTrayDTO.setAuditors(auditors);

        updateValues.put("auditors", auditors);

        doReturn(auditTray).when(auditTrayService).findById(1L);
        doNothing().when(validator).validate(any(), any());
        when(objectMapper.convertValue(updateValues, AuditTrayDTO.class)).thenReturn(auditTrayDTO);
        byte[] byteArr = new byte[0];
        when(objectMapper.writeValueAsBytes(any())).thenReturn(byteArr);
        when(objectMapper.readValue(byteArr, Auditor.class)).thenThrow(new IOException(""));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> auditTrayService.update(updateValues, 1L));

        assertThat(exception).hasMessageContaining("base.invalidFieldValue");
    }

    @Test
    public void testAuditLogsReturnsEmptyCollectionWhenAuditException() {
        doReturn(utils).when(beneficiaryService).getUtils();

        when(utils.getAuditReader()).thenReturn(auditReader);
        when(auditReader.createQuery()).thenThrow(new AuditException(""));

        List<AuditLog<AuditLogProjection<Long>, Long>> auditLogs = beneficiaryService.auditLogs(null, 1L);

        assertThat(auditLogs).isEmpty();
    }

    @Test
    public void testAuditLogsReturnsEmptyCollectionWhenAuditQueryReturnsEmptyRevision() {
        doReturn(utils).when(beneficiaryService).getUtils();

        AuditQuery query = mock(AuditQuery.class);
        AuditQueryCreator auditQueryCreator = mock(AuditQueryCreator.class);

        when(utils.getAuditReader()).thenReturn(auditReader);
        when(auditReader.createQuery()).thenReturn(auditQueryCreator);
        when(auditQueryCreator.forRevisionsOfEntityWithChanges(Beneficiary.class, true)).thenReturn(query);
        when(query.add(any())).thenReturn(query);
        when(query.addOrder(any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());


        List<AuditLog<AuditLogProjection<Long>, Long>> auditLogs = beneficiaryService.auditLogs(null, 1L);

        assertThat(auditLogs).isEmpty();
    }

    @Test
    public void testAuditLogsReturnsValidCollectionWhenAuditQueryReturnsRevisions() {
        doReturn(utils).when(beneficiaryService).getUtils();

        AuditQuery query = mock(AuditQuery.class);
        AuditQueryCreator auditQueryCreator = mock(AuditQueryCreator.class);

        when(utils.getAuditReader()).thenReturn(auditReader);
        when(auditReader.createQuery()).thenReturn(auditQueryCreator);
        when(auditQueryCreator.forRevisionsOfEntityWithChanges(Beneficiary.class, true)).thenReturn(query);
        when(query.add(any())).thenReturn(query);
        when(query.addOrder(any())).thenReturn(query);

        var revisionList = new ArrayList<Object[]>();
        var modifications = new HashSet<String>();

        Object[] revision = new Object[4];
        revision[0] = new Beneficiary();
        revision[2] = RevisionType.ADD;
        revision[3] = modifications;

        revisionList.add(revision);

        when(query.getResultList()).thenReturn(revisionList);


        List<AuditLog<AuditLogProjection<Long>, Long>> auditLogs = beneficiaryService.auditLogs(null, 1L);

        assertThat(auditLogs).isNotEmpty();
        assertThat(auditLogs.size()).isEqualTo(1);
        assertThat(auditLogs.get(0).getOperation()).isEqualTo(revision[2].toString());
        assertThat(auditLogs.get(0).getModifiedFields()).isEqualTo(revision[3]);
    }

    @Test
    public void testAuditLogsReturnsValidCollectionWhenAuditQueryReturnsRevisionsAndCustomProjection() {
        doReturn(utils).when(beneficiaryService).getUtils();

        AuditQuery query = mock(AuditQuery.class);
        AuditQueryCreator auditQueryCreator = mock(AuditQueryCreator.class);

        when(utils.getAuditReader()).thenReturn(auditReader);
        when(auditReader.createQuery()).thenReturn(auditQueryCreator);
        when(auditQueryCreator.forRevisionsOfEntityWithChanges(Beneficiary.class, true)).thenReturn(query);
        when(query.add(any())).thenReturn(query);
        when(query.addOrder(any())).thenReturn(query);

        var revisionList = new ArrayList<Object[]>();
        var modifications = new HashSet<String>();

        Object[] revision = new Object[4];
        revision[0] = new Beneficiary();
        revision[2] = RevisionType.ADD;
        revision[3] = modifications;

        revisionList.add(revision);

        when(query.getResultList()).thenReturn(revisionList);


        List<AuditLog<BeneficiaryAuditLogProjection, Long>> auditLogs = beneficiaryService.auditLogs(BeneficiaryAuditLogProjection.class, 1L);

        assertThat(auditLogs).isNotEmpty();
        assertThat(auditLogs.size()).isEqualTo(1);
        assertThat(auditLogs.get(0).getOperation()).isEqualTo(revision[2].toString());
        assertThat(auditLogs.get(0).getModifiedFields()).isEqualTo(revision[3]);
    }

    @Test
    public void testValidateReferenceReturnsEntityWhenIdExist() throws ObjectNotFoundException {
        long objectId = 1L;

        when(beneficiaryRepository.existsById(objectId)).thenReturn(true);

        Beneficiary result = beneficiaryService.validateReference(objectId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(objectId);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testValidateReferenceThrowsExceptionWhenIdDoesNotExist() throws ObjectNotFoundException {
        long objectId = 1L;

        when(beneficiaryRepository.existsById(objectId)).thenReturn(false);

        beneficiaryService.validateReference(objectId);
    }

}
