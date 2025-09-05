package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.security.JWTAuthenticationToken;
import com.capacidad.validationapi.misc.Utils;
import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.model.PaymentMethod;
import com.capacidad.validationapi.module.beneficiary.reference.PaymentMethodReference;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationItemService;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.model.NomenclatorConfig;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.render.service.RenderService;
import com.capacidad.validationapi.module.settlement.dto.SettlementDTO;
import com.capacidad.validationapi.module.settlement.dto.SettlementUpdateDTO;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import com.capacidad.validationapi.module.settlement.model.SettlementOperation;
import com.capacidad.validationapi.module.settlement.projection.SettlementProjection;
import com.capacidad.validationapi.module.settlement.repository.SettlementRepository;
import com.capacidad.validationapi.module.settlement.service.SettlementItemService;
import com.capacidad.validationapi.module.storage.dto.SummaryDTO;
import com.capacidad.validationapi.module.storage.model.FileType;
import com.capacidad.validationapi.module.storage.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_ADMIN_INSTANCE;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.ROLE_PRACTITIONER_INSTANCE;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SettlementServiceImplTest {

    @Mock
    private Utils utils;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JWTAuthenticationToken jwtAuthenticationToken;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private MedicalAuthorizationItemService medicalAuthorizationItemService;

    @Mock
    private StorageService storageService;

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private SettlementItemService settlementItemService;

    @Mock
    private RenderService renderService;

    @Spy
    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Before
    public void init() {
        when(settlementService.getUtils()).thenReturn(utils);
        when(settlementService.getObjectMapper()).thenReturn(new ObjectMapper());
        when(settlementService.getProjectionFactory()).thenReturn(new SpelAwareProxyProjectionFactory());
    }

    @Test
    public void testCreateThrowsExceptionWhenInvalidPractitionerContract() throws ObjectNotFoundException {
        SettlementDTO dto = new SettlementDTO();
        IdDTO<Long> practitionerDto = new IdDTO<>();
        IdDTO<Long> contractDto = new IdDTO<>();
        practitionerDto.setId(1L);
        contractDto.setId(2L);
        dto.setPractitioner(practitionerDto);
        dto.setContract(contractDto);

        Practitioner practitioner = new Practitioner();
        Contract practitionerContract = new Contract();
        practitionerContract.setId(3L);
        practitioner.getContracts().add(practitionerContract);

        when(practitionerService.findById(practitionerDto.getId())).thenReturn(practitioner);

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> settlementService.create(dto));

        assertThat(exception.getMessage()).isEqualTo("settlement.invalidPractitionerContract");
    }

    @Test
    public void testCreateThrowsExceptionWhenAlreadyOpenedSettlementForContractAndPractitioner() throws ObjectNotFoundException {
        SettlementDTO dto = new SettlementDTO();
        IdDTO<Long> practitionerDto = new IdDTO<>();
        IdDTO<Long> contractDto = new IdDTO<>();
        practitionerDto.setId(1L);
        contractDto.setId(2L);
        dto.setPractitioner(practitionerDto);
        dto.setContract(contractDto);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(practitionerDto.getId());
        Contract practitionerContract = new Contract();
        practitionerContract.setId(contractDto.getId());
        practitioner.getContracts().add(practitionerContract);

        when(practitionerService.findById(practitionerDto.getId())).thenReturn(practitioner);
        when(settlementRepository.findByPractitionerIdAndContractIdAndStatusId(practitionerDto.getId(), contractDto.getId(), OPEN_SETTLEMENT.getId()))
                .thenReturn(Optional.of(new Settlement()));

        ObjectAlreadyExistsException exception = (ObjectAlreadyExistsException) catchThrowable(() -> settlementService.create(dto));

        assertThat(exception.getMessage()).isEqualTo("settlement.alreadyExistsOpenedSettlement");
    }

    @Test
    public void testCreateReturnsSuccessfullyWhenNotOpenedSettlementForContractAndPractitioner() throws ObjectNotFoundException, ObjectNotValidException {
        SettlementDTO dto = new SettlementDTO();
        IdDTO<Long> practitionerDto = new IdDTO<>();
        IdDTO<Long> contractDto = new IdDTO<>();
        practitionerDto.setId(1L);
        contractDto.setId(2L);
        dto.setPractitioner(practitionerDto);
        dto.setContract(contractDto);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(practitionerDto.getId());
        Contract practitionerContract = new Contract();
        practitionerContract.setId(contractDto.getId());
        practitioner.getContracts().add(practitionerContract);

        Status opened = new Status();
        opened.setId(OPEN_SETTLEMENT.getId());

        when(practitionerService.findById(practitionerDto.getId())).thenReturn(practitioner);
        when(settlementRepository.findByPractitionerIdAndContractIdAndStatusId(practitionerDto.getId(), contractDto.getId(), OPEN_SETTLEMENT.getId()))
                .thenReturn(Optional.empty());
        when(utils.getGenericsEntityReference(Status.class, opened.getId())).thenReturn(opened);

        when(settlementRepository.save(any())).thenReturn(new Settlement());

        Settlement result = settlementService.create(dto);

        assertThat(result).isNotNull();
    }

    @Test
    public void testDeleteUpdatesAndRemoveItemsSuccessfully() throws ObjectNotFoundException {
        Settlement settlement = new Settlement();
        settlement.setId(1L);

        SettlementItem settlementItem = new SettlementItem();
        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setSettled(true);
        settlementItem.setMedicalAuthorizationItem(medicalAuthorizationItem);

        SettlementItem settlementItem2 = new SettlementItem();
        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setSettled(true);
        settlementItem2.setMedicalAuthorizationItem(medicalAuthorizationItem);

        settlement.getSettlementItems().add(settlementItem);
        settlement.getSettlementItems().add(settlementItem2);

        doReturn(settlement).when(settlementService).findById(settlement.getId());

        JsonNode result = settlementService.delete(settlement.getId());

        assertThat(result.get("id").asLong()).isEqualTo(settlement.getId());
        verify(medicalAuthorizationItemService, times(1)).saveAll(anyCollection());
        verify(settlementRepository, times(1)).delete(settlement);
        settlement.getSettlementItems().forEach(i -> assertThat(i.getMedicalAuthorizationItem().getSettled()).isFalse());
    }

    @Test
    public void testCreateOrUpdateFromMedicalAuthorizationExecuteSuccessfullyWhenPreviousSettlement() {
        Settlement settlement = new Settlement();
        settlement.setTotal(new BigDecimal(0));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Contract contract = new Contract();
        contract.setId(1L);
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setContract(contract);

        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(PaymentMethodReference.PAYCHECK.getId());
        beneficiary.setPaymentMethod(paymentMethod);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPaymentMethod(paymentMethod);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        NomenclatorConfig nomenclatorConfig = new NomenclatorConfig();
        nomenclatorConfig.setExpirationDays(60);
        nomenclatorConfig.setReportRequired(false);
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorConfig(nomenclatorConfig);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(3L);
        medicalAuthorizationItem.setCreatedAt(LocalDateTime.now());
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setSettled(false);
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(125));
        medicalAuthorizationItem.setUnitPrice(new BigDecimal(125));
        medicalAuthorizationItem.setQuantity(1);
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        MedicalAuthorizationItem medicalAuthorizationItem2 = new MedicalAuthorizationItem();
        medicalAuthorizationItem2.setId(4L);
        medicalAuthorizationItem2.setCreatedAt(LocalDateTime.now());
        medicalAuthorizationItem2.setNomenclator(nomenclator);
        medicalAuthorizationItem2.setSettled(false);
        medicalAuthorizationItem2.setStatus(rejected);
        medicalAuthorizationItem2.setSubtotal(new BigDecimal(125));
        medicalAuthorizationItem2.setUnitPrice(new BigDecimal(125));
        medicalAuthorizationItem2.setQuantity(1);
        medicalAuthorizationItem2.setMedicalAuthorization(medicalAuthorization);

        MedicalAuthorizationItem medicalAuthorizationItem3 = new MedicalAuthorizationItem();
        medicalAuthorizationItem3.setId(5L);
        medicalAuthorizationItem3.setNomenclator(nomenclator);
        medicalAuthorizationItem3.setCreatedAt(LocalDateTime.now());
        medicalAuthorizationItem3.setSettled(true);
        medicalAuthorizationItem3.setStatus(approved);
        medicalAuthorizationItem3.setSubtotal(new BigDecimal(125));
        medicalAuthorizationItem3.setUnitPrice(new BigDecimal(125));
        medicalAuthorizationItem3.setQuantity(1);
        medicalAuthorizationItem3.setMedicalAuthorization(medicalAuthorization);

        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem2);
        medicalAuthorization.getMedicalAuthorizationItems().add(medicalAuthorizationItem3);

        when(settlementRepository.findByPractitionerIdAndContractIdAndStatusId(practitioner.getId(), contract.getId(), OPEN_SETTLEMENT.getId())).thenReturn(Optional.of(settlement));

        settlementService.createOrUpdateFromMedicalAuthorization(medicalAuthorization);

        assertThat(medicalAuthorizationItem.getSettled()).isTrue();
        assertThat(settlement.getTotal()).isEqualTo(medicalAuthorizationItem.getSubtotal());
        assertThat(settlement.getSettlementItems().size()).isEqualTo(1);
        verify(settlementRepository, times(1)).save(settlement);
    }

    @Test
    public void testCreateOrUpdateFromMedicalAuthorizationItemExecuteSuccessfullyWithoutPreviousSettlement() {
        Settlement settlement = new Settlement();
        settlement.setTotal(new BigDecimal(0));

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        Contract contract = new Contract();
        contract.setId(1L);
        Practitioner practitioner = new Practitioner();
        practitioner.setId(2L);
        medicalAuthorization.setPractitioner(practitioner);
        medicalAuthorization.setContract(contract);

        Beneficiary beneficiary = new Beneficiary();
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(PaymentMethodReference.PAYCHECK.getId());
        beneficiary.setPaymentMethod(paymentMethod);

        medicalAuthorization.setBeneficiary(beneficiary);
        medicalAuthorization.setPaymentMethod(paymentMethod);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Status openSettlement = new Status();
        openSettlement.setId(OPEN_SETTLEMENT.getId());

        NomenclatorConfig nomenclatorConfig = new NomenclatorConfig();
        nomenclatorConfig.setExpirationDays(60);
        nomenclatorConfig.setReportRequired(false);
        Nomenclator nomenclator = new Nomenclator();
        nomenclator.setNomenclatorConfig(nomenclatorConfig);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(3L);
        medicalAuthorizationItem.setCreatedAt(LocalDateTime.now());
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setSettled(false);
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(125));
        medicalAuthorizationItem.setUnitPrice(new BigDecimal(125));
        medicalAuthorizationItem.setQuantity(1);
        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        when(settlementRepository.findByPractitionerIdAndContractIdAndStatusId(practitioner.getId(), contract.getId(), OPEN_SETTLEMENT.getId())).thenReturn(Optional.empty());
        when(utils.getGenericsEntityReference(Status.class, OPEN_SETTLEMENT.getId())).thenReturn(openSettlement);

        settlementService.createOrUpdateFromMedicalAuthorizationItem(medicalAuthorizationItem);

        assertThat(medicalAuthorizationItem.getSettled()).isTrue();
        verify(settlementRepository, times(1)).save(any(Settlement.class));
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenNotHighRankingAuthorityAndClosedSettlement() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        settlement.setStatus(closed);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_PRACTITIONER_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), new SettlementUpdateDTO()));

        assertThat(exception.getMessage()).isEqualTo("settlement.cannotOperateOverClosedSettlement");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenRemoveStrategyAndNullDTOIds() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        settlement.setStatus(closed);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.REMOVE_ITEMS);
        updateDTO.setItemIds(null);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).isEqualTo("settlement.emptyItems");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenRemoveStrategyAndEmptyDTOIds() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        settlement.setStatus(closed);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.REMOVE_ITEMS);
        updateDTO.setItemIds(new HashSet<>());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).isEqualTo("settlement.emptyItems");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementExecuteSuccessfullyWhenRemoveStrategyAndValidDTO() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setSettled(true);

        SettlementItem settlementItem = new SettlementItem();
        settlementItem.setSubtotal(new BigDecimal(250));
        settlementItem.setId(2L);
        settlementItem.setMedicalAuthorizationItem(medicalAuthorizationItem);

        settlement.getSettlementItems().add(settlementItem);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(settlementItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.REMOVE_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(settlementRepository.save(settlement)).thenReturn(settlement);

        SettlementProjection result = settlementService.updateSettlement(settlement.getId(), updateDTO);

        assertThat(result).isNotNull();
        assertThat(settlement.getTotal()).isEqualTo(new BigDecimal(250));
        assertThat(settlement.getSettlementItems().size()).isZero();
        assertThat(medicalAuthorizationItem.getSettled()).isFalse();
        verify(medicalAuthorizationItemService, times(1)).saveAll(anyCollection());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenAddStrategyAndInvalidItemCorrespondence() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(1L);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.emptySet());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).isEqualTo("settlement.invalidItems");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenAddStrategyAndItemAlreadySettled() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setSettled(true);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(medicalAuthorizationItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.singleton(medicalAuthorizationItem));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).contains("settlement.alreadySettled");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenAddStrategyAndNullItemSubtotal() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setSubtotal(null);
        medicalAuthorizationItem.setSettled(false);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(medicalAuthorizationItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.singleton(medicalAuthorizationItem));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).contains("settlement.invalidSubtotal");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenAddStrategyAndZeroItemSubtotal() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(0));
        medicalAuthorizationItem.setSettled(false);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(medicalAuthorizationItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.singleton(medicalAuthorizationItem));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).contains("settlement.invalidSubtotal");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenAddStrategyAndItemNotApproved() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        Status rejected = new Status();
        rejected.setId(VALIDATION_REJECTED.getId());

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setStatus(rejected);
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(123));
        medicalAuthorizationItem.setSettled(false);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(medicalAuthorizationItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.singleton(medicalAuthorizationItem));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).contains("settlement.invalidItemStatus");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenAddStrategyAndExpiredItem() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Nomenclator nomenclator = new Nomenclator();
        NomenclatorConfig nomenclatorConfig = new NomenclatorConfig();
        nomenclatorConfig.setExpirationDays(30);
        nomenclator.setNomenclatorConfig(nomenclatorConfig);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setCreatedAt(LocalDateTime.now().minusDays(nomenclatorConfig.getExpirationDays() + 1));
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(123));
        medicalAuthorizationItem.setSettled(false);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(medicalAuthorizationItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.singleton(medicalAuthorizationItem));

        RuntimeException exception = (RuntimeException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).contains("settlement.expiredItem");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenAddStrategyAndReportRequired() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());
        settlement.setTotal(new BigDecimal(500));
        settlement.setId(1L);
        settlement.setStatus(closed);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Nomenclator nomenclator = new Nomenclator();
        NomenclatorConfig nomenclatorConfig = new NomenclatorConfig();
        nomenclatorConfig.setExpirationDays(30);
        nomenclatorConfig.setReportRequired(true);
        nomenclator.setNomenclatorConfig(nomenclatorConfig);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setCreatedAt(LocalDateTime.now());
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setSubtotal(new BigDecimal(123));
        medicalAuthorizationItem.setSettled(false);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(medicalAuthorizationItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.singleton(medicalAuthorizationItem));
        when(storageService.getFileList(FileType.REPORT, medicalAuthorization.getId())).thenReturn(Collections.emptyList());

        RuntimeException exception = (RuntimeException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).contains("settlement.reportRequired");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementReturnsProjectionWhenAddStrategyAndValidDTOVoluntaryItems() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        Settlement settlement = new Settlement();
        settlement.setPractitioner(new Practitioner());
        settlement.setContract(new Contract());

        BigDecimal settlementTotal = new BigDecimal(500);

        settlement.setTotal(settlementTotal);
        settlement.setId(1L);
        settlement.setStatus(closed);

        Status approved = new Status();
        approved.setId(VALIDATION_APPROVED.getId());

        Nomenclator nomenclator = new Nomenclator();
        NomenclatorConfig nomenclatorConfig = new NomenclatorConfig();
        nomenclatorConfig.setExpirationDays(30);
        nomenclatorConfig.setReportRequired(true);
        nomenclator.setNomenclatorConfig(nomenclatorConfig);

        MedicalAuthorizationItem medicalAuthorizationItem = new MedicalAuthorizationItem();
        medicalAuthorizationItem.setNomenclator(nomenclator);
        medicalAuthorizationItem.setCreatedAt(LocalDateTime.now());
        medicalAuthorizationItem.setStatus(approved);
        medicalAuthorizationItem.setId(1L);
        medicalAuthorizationItem.setUnitPrice(new BigDecimal(100));
        medicalAuthorizationItem.setSubtotal(new BigDecimal(200));
        medicalAuthorizationItem.setQuantity(2);
        medicalAuthorizationItem.setChargeSubtotal(new BigDecimal(100));
        medicalAuthorizationItem.setChargeUnitPrice(new BigDecimal(50));
        medicalAuthorizationItem.setSettled(false);

        MedicalAuthorization medicalAuthorization = new MedicalAuthorization();
        medicalAuthorization.setId(1L);
        PaymentMethod voluntary = new PaymentMethod();
        voluntary.setId(PaymentMethodReference.VOLUNTARY.getId());
        medicalAuthorization.setPaymentMethod(voluntary);
        medicalAuthorization.setMedicalCenter(new MedicalCenter());

        medicalAuthorizationItem.setMedicalAuthorization(medicalAuthorization);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(medicalAuthorizationItem.getId());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.ADD_ITEMS);
        updateDTO.setItemIds(itemIds);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(medicalAuthorizationItemService.findNotSettledItemsByPractitionerContractAndCollectionOfIds
                (settlement.getPractitioner(), settlement.getContract(), updateDTO.getItemIds())).thenReturn(Collections.singleton(medicalAuthorizationItem));
        when(storageService.getFileList(FileType.REPORT, medicalAuthorization.getId())).thenReturn(Collections.singletonList(new SummaryDTO()));
        when(settlementRepository.save(settlement)).thenReturn(settlement);

        SettlementProjection result = settlementService.updateSettlement(settlement.getId(), updateDTO);

        assertThat(result).isNotNull();
        SettlementItem settlementItem = settlement.getSettlementItems().iterator().next();
        assertThat(settlementItem.getMedicalAuthorization()).isEqualTo(medicalAuthorization);
        assertThat(settlementItem.getMedicalAuthorizationItem()).isEqualTo(medicalAuthorizationItem);
        assertThat(settlementItem.getUnitPrice()).isEqualTo(medicalAuthorizationItem.getUnitPrice());
        assertThat(settlementItem.getQuantity()).isEqualTo(medicalAuthorizationItem.getQuantity());
        assertThat(settlementItem.getChargeUnitPrice()).isEqualTo(medicalAuthorizationItem.getChargeUnitPrice());
        assertThat(settlementItem.getSubtotal()).isEqualTo(medicalAuthorizationItem.getSubtotal()
                .subtract(medicalAuthorizationItem.getChargeSubtotal())
                .setScale(2, RoundingMode.HALF_UP));
        assertThat(settlementItem.getNomenclator()).isEqualTo(medicalAuthorizationItem.getNomenclator());
        assertThat(settlementItem.getMedicalCenter()).isEqualTo(medicalAuthorization.getMedicalCenter());
        assertThat(settlement.getTotal()).isEqualTo(settlementTotal.add(settlementItem.getSubtotal()));
        verify(medicalAuthorizationItemService, times(1)).saveAll(anyCollection());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenCloseStrategyAndClosedSettlement() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());
        settlement.setStatus(closed);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.CLOSE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).isEqualTo("settlement.invalidStatus");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenInvalidClosingPeriod() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status opened = new Status();
        opened.setId(OPEN_SETTLEMENT.getId());
        settlement.setStatus(opened);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        Contract contract = new Contract();
        contract.setId(1L);

        settlement.setPractitioner(practitioner);
        settlement.setContract(contract);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.CLOSE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_PRACTITIONER_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(settlementRepository.existsByIdIsNotAndPractitionerIdAndStatusIdAndContractIdAndClosedAtBetween
                (anyLong(), anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).isEqualTo("settlement.alreadyClosed");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenEmptyItems() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status opened = new Status();
        opened.setId(OPEN_SETTLEMENT.getId());
        settlement.setStatus(opened);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        Contract contract = new Contract();
        contract.setId(1L);

        settlement.setPractitioner(practitioner);
        settlement.setContract(contract);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.CLOSE);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_PRACTITIONER_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(settlementRepository.existsByIdIsNotAndPractitionerIdAndStatusIdAndContractIdAndClosedAtBetween
                (anyLong(), anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).isEqualTo("settlement.emptySettlement");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementReturnsProjectionWhenCloseStrategyAndValidSettlement() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status opened = new Status();
        opened.setId(OPEN_SETTLEMENT.getId());
        settlement.setStatus(opened);
        settlement.getSettlementItems().add(new SettlementItem());

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        Contract contract = new Contract();
        contract.setId(1L);

        settlement.setPractitioner(practitioner);
        settlement.setContract(contract);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.CLOSE);

        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_PRACTITIONER_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(settlementRepository.existsByIdIsNotAndPractitionerIdAndStatusIdAndContractIdAndClosedAtBetween
                (anyLong(), anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(utils.getGenericsEntityReference(Status.class, closed.getId())).thenReturn(closed);
        when(settlementRepository.save(settlement)).thenReturn(settlement);

        SettlementProjection result = settlementService.updateSettlement(settlement.getId(), updateDTO);

        assertThat(result).isNotNull();
        assertThat(settlement.getClosedAt().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(settlement.getStatus().getId()).isEqualTo(closed.getId());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementReturnsProjectionWithoutChangesWhenUpdateStrategyAndNotHighRanking() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status opened = new Status();
        opened.setId(OPEN_SETTLEMENT.getId());
        settlement.setStatus(opened);
        settlement.setOpenedAt(LocalDateTime.now().minusMonths(1));

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.UPDATE);
        updateDTO.setOpenedAt(LocalDateTime.now().minusMonths(2));

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_PRACTITIONER_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());

        SettlementProjection result = settlementService.updateSettlement(settlement.getId(), updateDTO);

        assertThat(result).isNotNull();
        assertThat(settlement.getOpenedAt()).isNotEqualTo(updateDTO.getOpenedAt());
        verify(settlementRepository, never()).save(settlement);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementReturnsProjectionWithoutChangesWhenUpdateStrategyAndNullDatesAndHighRanking() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status opened = new Status();
        opened.setId(OPEN_SETTLEMENT.getId());
        settlement.setStatus(opened);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.UPDATE);
        updateDTO.setOpenedAt(null);
        updateDTO.setClosedAt(null);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(settlementRepository.save(settlement)).thenReturn(settlement);

        SettlementProjection result = settlementService.updateSettlement(settlement.getId(), updateDTO);

        assertThat(result).isNotNull();
        verify(settlementRepository, times(1)).save(settlement);

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementReturnsProjectionWithOpenedAtUpdatedWhenUpdateStrategyAndHighRanking() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status opened = new Status();
        opened.setId(OPEN_SETTLEMENT.getId());
        settlement.setStatus(opened);
        settlement.setOpenedAt(LocalDateTime.now().minusMonths(1));

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.UPDATE);
        updateDTO.setOpenedAt(LocalDateTime.now().minusMonths(2));

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(settlementRepository.save(settlement)).thenReturn(settlement);

        SettlementProjection result = settlementService.updateSettlement(settlement.getId(), updateDTO);

        assertThat(result).isNotNull();
        assertThat(settlement.getOpenedAt()).isEqualTo(updateDTO.getOpenedAt());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenNegativeDatesDuration() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());
        settlement.setStatus(closed);
        settlement.setOpenedAt(LocalDateTime.now().minusMonths(1));
        settlement.setClosedAt(LocalDateTime.now());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.UPDATE);
        updateDTO.setOpenedAt(LocalDateTime.now().minusMonths(2));
        updateDTO.setClosedAt(LocalDateTime.now().minusMonths(3));

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));


        assertThat(exception.getMessage()).isEqualTo("settlement.openedAtBiggerOrEqualClosedAt");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementThrowsExceptionWhenZeroDatesDuration() throws ObjectNotFoundException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());
        settlement.setStatus(closed);
        settlement.setOpenedAt(LocalDateTime.now().minusMonths(1));
        settlement.setClosedAt(LocalDateTime.now());

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.UPDATE);
        updateDTO.setOpenedAt(LocalDate.now().minusMonths(2).atTime(0, 0, 0, 0));
        updateDTO.setClosedAt(LocalDate.now().minusMonths(2).atTime(0, 0, 0, 0));

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.updateSettlement(settlement.getId(), updateDTO));

        assertThat(exception.getMessage()).isEqualTo("settlement.openedAtBiggerOrEqualClosedAt");

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testUpdateSettlementReturnsProjectionWithBothDatesUpdatedWhenUpdateStrategyAndHighRanking() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        Status closed = new Status();
        closed.setId(CLOSED_SETTLEMENT.getId());
        settlement.setStatus(closed);
        settlement.setOpenedAt(LocalDateTime.now().minusMonths(1));
        settlement.setClosedAt(LocalDateTime.now());

        Practitioner practitioner = new Practitioner();
        practitioner.setId(1L);

        Contract contract = new Contract();
        contract.setId(1L);

        settlement.setPractitioner(practitioner);
        settlement.setContract(contract);

        SettlementUpdateDTO updateDTO = new SettlementUpdateDTO();
        updateDTO.setOperation(SettlementOperation.UPDATE);
        updateDTO.setOpenedAt(LocalDateTime.now().minusMonths(3));
        updateDTO.setClosedAt(LocalDateTime.now().minusMonths(2));

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getAuthorities()).thenReturn(Collections.singletonList(ROLE_ADMIN_INSTANCE));
        doReturn(settlement).when(settlementService).findById(settlement.getId());
        when(settlementRepository.save(settlement)).thenReturn(settlement);
        when(settlementRepository.existsByIdIsNotAndPractitionerIdAndStatusIdAndContractIdAndClosedAtBetween
                (anyLong(), anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        SettlementProjection result = settlementService.updateSettlement(settlement.getId(), updateDTO);

        assertThat(result).isNotNull();
        assertThat(settlement.getOpenedAt()).isEqualTo(updateDTO.getOpenedAt());
        assertThat(settlement.getClosedAt()).isEqualTo(updateDTO.getClosedAt());

        SecurityContextHolder.setContext(defaultContext);
    }

    @Test
    public void testCloseAllIfContractDayMatchesDoNotSaveAllWhenEmptyOpenedSettlements() {
        when(settlementRepository.findAllByStatusIdAndContractAutoSettlementIsTrue(OPEN_SETTLEMENT.getId())).thenReturn(Collections.emptySet());

        settlementService.closeAllIfContractDayMatchesDate(LocalDate.now());

        verify(settlementRepository, never()).saveAll(anyCollection());
    }

    @Test
    public void testCloseAllIfContractDayMatchesDateDoNotSaveAllWhenNotClosingDay() {
        Settlement settlement = new Settlement();
        Contract contract = new Contract();
        contract.setDayOfSettlement(LocalDate.now().plusDays(1).getDayOfMonth());
        settlement.setContract(contract);

        when(settlementRepository.findAllByStatusIdAndContractAutoSettlementIsTrue(OPEN_SETTLEMENT.getId())).thenReturn(Collections.singleton(settlement));

        settlementService.closeAllIfContractDayMatchesDate(LocalDate.now().minusDays(1));

        verify(settlementRepository, never()).saveAll(anyCollection());
    }

    @Test
    public void testCloseAllIfContractDayMatchesDateDoNotSaveAllWhenNotClosingDayAndLastDayOfMonth() {
        Settlement settlement = new Settlement();
        Contract contract = new Contract();
        contract.setDayOfSettlement(31);
        settlement.setContract(contract);

        when(settlementRepository.findAllByStatusIdAndContractAutoSettlementIsTrue(OPEN_SETTLEMENT.getId())).thenReturn(Collections.singleton(settlement));

        settlementService.closeAllIfContractDayMatchesDate(LocalDate.now().minusDays(1));

        verify(settlementRepository, never()).saveAll(anyCollection());
    }

    @Test
    public void testCloseAllIfContractDayMatchesDateSavesAllWhenClosingDay() {
        Settlement settlement = new Settlement();
        Contract contract = new Contract();
        contract.setDayOfSettlement(LocalDate.now().getDayOfMonth());
        settlement.setContract(contract);

        when(settlementRepository.findAllByStatusIdAndContractAutoSettlementIsTrue(OPEN_SETTLEMENT.getId())).thenReturn(Collections.singleton(settlement));

        settlementService.closeAllIfContractDayMatchesDate(LocalDate.now());

        assertThat(settlement.getClosedAt()).isNotNull();
        assertThat(settlement.getStatus().getId()).isEqualTo(CLOSED_SETTLEMENT.getId());
        verify(settlementRepository, times(1)).saveAll(anyCollection());
    }

    @Test
    public void testGenerateItemsReceiptThrowsExceptionWhenItemsNotFound() {
        Set<Long> itemIds = new HashSet<>();
        itemIds.add(1L);
        itemIds.add(2L);

        when(settlementItemService.findAllBySettlementIdAndItemIds(1L, itemIds)).thenReturn(Collections.emptyList());

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> settlementService.generateItemsReceipt(1L, itemIds));

        assertThat(exception.getMessage()).isEqualTo("settlement.emptyItems");
    }

    @Test
    public void testGenerateItemsReceiptDoNotFailsWhenItemsFound() throws ObjectNotValidException {
        SecurityContext defaultContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(securityContext);

        ByteArrayOutputStream expectedResult = mock(ByteArrayOutputStream.class);

        Set<Long> itemIds = new HashSet<>();
        itemIds.add(1L);
        itemIds.add(2L);

        Settlement settlement = new Settlement();
        settlement.setId(1L);

        SettlementItem settlementItem = new SettlementItem();
        settlementItem.setSubtotal(new BigDecimal(123));
        settlementItem.setSettlement(settlement);
        SettlementItem settlementItem1 = new SettlementItem();
        settlementItem1.setSubtotal(new BigDecimal(124));
        settlementItem1.setSettlement(settlement);

        List<SettlementItem> settlementItems = new ArrayList<>();
        settlementItems.add(settlementItem);
        settlementItems.add(settlementItem1);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getPrincipal()).thenReturn("principal");
        when(settlementItemService.findAllBySettlementIdAndItemIds(settlement.getId(), itemIds)).thenReturn(settlementItems);
        when(renderService.renderPDF(anyString(), anyMap())).thenReturn(expectedResult);

        ByteArrayOutputStream result = settlementService.generateItemsReceipt(settlement.getId(), itemIds);
        assertThat(result).isEqualTo(expectedResult);

        SecurityContextHolder.setContext(defaultContext);
    }

}
