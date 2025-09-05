package com.capacidad.validationapi.prescription.galbop.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicine.model.Medicine;
import com.capacidad.validationapi.module.practitioner.model.MedicalRegistration;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
import com.capacidad.validationapi.prescription.galbop.model.*;
import com.capacidad.validationapi.prescription.galbop.projection.GalbopBeneficiaryProjection;
import com.capacidad.validationapi.prescription.galbop.repository.GalbopRepository;
import com.capacidad.validationapi.prescription.galbop.service.GalbopPrescriptionValidator;
import com.capacidad.validationapi.prescription.integration.ProductIntegrationProjection;
import com.capacidad.validationapi.prescription.integration.ProductWrapperDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static com.capacidad.validationapi.prescription.galbop.service.impl.GalbopPrescriptionServiceImpl.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GalbopPrescriptionServiceImplTest {

    @Mock
    private GalbopRepository galbopRepository;

    @Mock
    private PrescriptionService prescriptionService;

    @Mock
    private GalbopPrescriptionValidator galbopPrescriptionValidator;

    @InjectMocks
    private GalbopPrescriptionServiceImpl galbopService;

    @Test
    public void testParseBeneficiaryCodeWhenTypeEnding00() {
        String beneficiaryCode = "03-30529599/00";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("3305295990");
    }

    @Test
    public void testParseBeneficiaryCodeWhenTypeNotEnding0() {
        String beneficiaryCode = "03-13462981/11";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("31346298111");
    }

    @Test
    public void testParseBeneficiaryCodeWhenTypeEnding0() {
        String beneficiaryCode = "03-13462918/80";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("31346291880");
    }

    @Test
    public void testParseBeneficiaryCodeWhenTypeContainsButNotEnding0() {
        String beneficiaryCode = "03-13899861/01";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("3138998611");
    }

    @Test
    public void testParseBeneficiaryCodeWhenStartingZeroAndEnding00() {
        String beneficiaryCode = "02-05397428/00";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("253974280");
    }

    @Test
    public void testParseBeneficiaryCodeWhenStartingMultipleZeroAndEnding00() {
        String beneficiaryCode = "02-00942380/00";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("29423800");
    }

    @Test
    public void testParseBeneficiaryCodeWhenInnerZeroAndNotEnding00() {
        String beneficiaryCode = "02-05006022/70";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("2500602270");
    }

    @Test
    public void testParseBeneficiaryCodeWhenInnerZeroAndStartingZero() {
        String beneficiaryCode = "02-05409360/06";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("254093606");
    }

    @Test
    public void testParseBeneficiaryCodeWhenInnerZeroAndNotEndingZero() {
        String beneficiaryCode = "02-09997254/63";
        String result = galbopService.parseBeneficiaryCode(beneficiaryCode);
        assertThat(result).isEqualTo("2999725463");
    }

    @Test
    public void testGetProductsReturnsEmptyWhenInvalidMapping() {
        GalbopBeneficiaryProjection beneficiaryProjection = mock(GalbopBeneficiaryProjection.class);
        when(beneficiaryProjection.getPlanBehaviour()).thenReturn(11111);
        List<GalbopBeneficiaryProjection> beneficiaryProjections = Collections.singletonList(beneficiaryProjection);

        String search = "search";

        when(galbopRepository.findBeneficiaryGroupedPlans("3305295990")).thenReturn(beneficiaryProjections);
        when(galbopRepository.findProductsByNameAndVademecumIds(StringUtils.join(PERCENT_SIGN, search, PERCENT_SIGN), GalbopPlanMapping.DEFAULT.getVademecumIds())).thenReturn(Collections.emptySet());

        ProductWrapperDTO result = galbopService.getProducts(search, "03-30529599/00");

        assertThat(result.getProducts()).isEmpty();
        assertThat(result.getPlanId()).isZero();
    }

    @Test
    public void testGetProductsReturnsEmptyWhenValidMappingButNonexistentBeneficiaryPlanVademecum() {
        GalbopBeneficiaryProjection beneficiaryProjection = mock(GalbopBeneficiaryProjection.class);
        when(beneficiaryProjection.getPlanBehaviour()).thenReturn(306);
        List<GalbopBeneficiaryProjection> beneficiaryProjections = Collections.singletonList(beneficiaryProjection);

        String search = "search";

        when(galbopRepository.findBeneficiaryGroupedPlans("3305295990")).thenReturn(beneficiaryProjections);
        when(galbopRepository.findProductsByNameAndVademecumIds(StringUtils.join(PERCENT_SIGN, search, PERCENT_SIGN), GalbopPlanMapping.PLAN_BEHAVIOUR_306.getVademecumIds())).thenReturn(Collections.emptySet());

        ProductWrapperDTO result = galbopService.getProducts(search, "03-30529599/00");

        assertThat(result.getProducts()).isEmpty();
        assertThat(result.getPlanId()).isZero();
    }

    @Test
    public void testGetProductsReturnProductsWhenValidMappingAndExistentBeneficiaryPlanVademecum() {
        GalbopBeneficiaryProjection beneficiaryProjection = mock(GalbopBeneficiaryProjection.class);
        when(beneficiaryProjection.getPlanBehaviour()).thenReturn(306);
        when(beneficiaryProjection.getPlanId()).thenReturn(1);
        List<GalbopBeneficiaryProjection> beneficiaryProjections = Collections.singletonList(beneficiaryProjection);

        ProductIntegrationProjection product = mock(ProductIntegrationProjection.class);
        Set<ProductIntegrationProjection> expected = Collections.singleton(product);

        String search = "search";

        when(galbopRepository.findBeneficiaryGroupedPlans("3305295990")).thenReturn(beneficiaryProjections);
        when(galbopRepository.findProductsByNameAndVademecumIds(StringUtils.join(PERCENT_SIGN, search, PERCENT_SIGN), GalbopPlanMapping.PLAN_BEHAVIOUR_306.getVademecumIds())).thenReturn(expected);

        ProductWrapperDTO result = galbopService.getProducts(search, "03-30529599/00");

        assertThat(result.getProducts()).isEqualTo(expected);
        assertThat(result.getPlanId()).isEqualTo(beneficiaryProjection.getPlanId());
    }

    @Test
    public void testSavePrescriptionIsNotExecutedWhenEmptyPrescription() throws ObjectNotFoundException, ObjectNotValidException {
        galbopService.savePrescription(new Prescription());

        verify(galbopRepository, never()).getLastNumeration();
    }

    @Test
    public void testSavePrescriptionsIsNotExecutedWhenEmptyPrescriptionList() throws ObjectNotValidException, ObjectNotFoundException {
        galbopService.savePrescriptions(Collections.singleton(new Prescription()));

        verify(galbopRepository, never()).getLastNumeration();
    }

    @Test
    public void testSavePrescriptionExecutesUpdateWhenExchangeIdIsPresent() throws ObjectNotFoundException, ObjectNotValidException {
        String prescriptionNumber = "201920192019";

        Prescription prescription = new Prescription();
        prescription.getExchangeId().add(Long.valueOf(prescriptionNumber));

        String beneficiaryCode = "03-30529599/00";
        String parsedBeneficiaryCode = "3305295990";

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode(beneficiaryCode);

        Practitioner practitioner = new Practitioner();
        practitioner.setLastName("lastName");
        practitioner.setName("name");
        MedicalRegistration medicalRegistration = new MedicalRegistration();
        medicalRegistration.setRegistrationCode("MP 1122");
        practitioner.getMedicalRegistrations().add(medicalRegistration);

        ICD10Disease icd10Disease = new ICD10Disease();
        icd10Disease.setCode("A2");

        PrescriptionItem prescriptionItem = new PrescriptionItem();
        prescriptionItem.setDisease(icd10Disease);

        prescription.setBeneficiary(beneficiary);
        prescription.setPractitioner(practitioner);
        prescription.getPrescriptionItems().add(prescriptionItem);

        GalbopPrescriptionDetail galbopPrescriptionDetail = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail.setDateTo(LocalDate.now().plusDays(1));
        GalbopPrescription galbopPrescription = new GalbopPrescription();
        galbopPrescriptionDetail.setPrescription(galbopPrescription);

        when(galbopRepository.findValidatedPrescriptionDetail
                (parsedBeneficiaryCode, GALBOP_PREAUTHORIZED_PLAN_ID, prescriptionNumber))
                .thenReturn(Optional.of(galbopPrescriptionDetail));

        galbopService.savePrescription(prescription);

        assertThat(prescription.getPreAuthorized()).isTrue();
        assertThat(prescription.getExpirationDate()).isEqualTo(galbopPrescriptionDetail.getDateTo());
        assertThat(galbopPrescription.getRegistration()).isEqualTo(1122);
        assertThat(galbopPrescription.getPractitioner()).isEqualTo("DR. LASTNAME, NAME");
        assertThat(galbopPrescription.getPractitionerType()).isEqualTo(GALBOP_DEFAULT_PRACTITIONER_TYPE);
        assertThat(galbopPrescription.getRegistrationProvince()).isEqualTo(GALBOP_DEFAULT_REGISTRATION_PROVINCE);
        assertThat(galbopPrescription.getDisease()).isEqualTo(icd10Disease.getCode());
        verify(galbopRepository, times(1)).savePrescriptionDetail(galbopPrescriptionDetail);
        verify(prescriptionService, times(1)).save(prescription);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSavePrescriptionExecutesUpdateAndThrowsExceptionWhenExchangeIdIsPresentButInvalid() throws ObjectNotFoundException, ObjectNotValidException {
        String prescriptionNumber = "201920192019";

        Prescription prescription = new Prescription();
        prescription.getExchangeId().add(Long.valueOf(prescriptionNumber));

        String beneficiaryCode = "03-30529599/00";
        String parsedBeneficiaryCode = "3305295990";

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode(beneficiaryCode);

        prescription.setBeneficiary(beneficiary);

        when(galbopRepository.findValidatedPrescriptionDetail
                (parsedBeneficiaryCode, GALBOP_PREAUTHORIZED_PLAN_ID, prescriptionNumber))
                .thenReturn(Optional.empty());

        galbopService.savePrescription(prescription);
    }

    @Test
    public void testSavePrescriptionWithNonMixedPlanPersistPrescriptionCorrectly() throws ObjectNotFoundException, ObjectNotValidException {
        GalbopPrescriptionNumeration lastNumberation = new GalbopPrescriptionNumeration();
        lastNumberation.setNumeration(2019010201L);

        GalbopPrescriptionNumeration newNumeration = new GalbopPrescriptionNumeration();
        newNumeration.setNumeration(lastNumberation.getNumeration() + 1);

        when(galbopRepository.getLastNumeration()).thenReturn(lastNumberation);
        when(galbopRepository.getNextPrescriptionId()).thenReturn(1L);

        Prescription prescription = buildPrescription();
        Set<PrescriptionItem> prescriptionItems = buildRegularPrescriptionItems();
        PrescriptionItem prescriptionItem1 = prescriptionItems.iterator().next();
        prescription.setPrescriptionItems(prescriptionItems);
        prescription.associateChildObjects();

        int uniqueMedicinePlanId = prescriptionItem1.getMedicine().getExchangePlanId().intValue();
        GalbopPrescription expectedPrescription = galbopService.createPrescription(uniqueMedicinePlanId, newNumeration, prescription, prescriptionItems);
        doNothing().when(prescriptionService).save(prescription);

        galbopService.savePrescription(prescription);

        assertPrescription(expectedPrescription, newNumeration, prescription, uniqueMedicinePlanId);
        assertThat(prescription.getExchangeId()).contains(newNumeration.getNumeration());
        verify(galbopRepository, times(1)).savePrescription(any());
        verify(galbopPrescriptionValidator, times(1)).validate(any(GalbopPrescription.class), any(Prescription.class));
    }

    @Test
    public void testSavePrescriptionWithMixedPlanPersistMultiplePrescriptionsCorrectly() throws ObjectNotFoundException, ObjectNotValidException {
        GalbopPrescriptionNumeration lastNumeration1 = new GalbopPrescriptionNumeration();
        lastNumeration1.setNumeration(2019010203L);

        GalbopPrescriptionNumeration newNumeration1 = new GalbopPrescriptionNumeration();
        newNumeration1.setNumeration(lastNumeration1.getNumeration() + 1);

        GalbopPrescriptionNumeration lastNumeration2 = new GalbopPrescriptionNumeration();
        lastNumeration2.setNumeration(2019010204L);

        GalbopPrescriptionNumeration newNumeration2 = new GalbopPrescriptionNumeration();
        newNumeration2.setNumeration(lastNumeration2.getNumeration() + 1);

        when(galbopRepository.getLastNumeration()).thenReturn(lastNumeration1).thenReturn(lastNumeration2);
        when(galbopRepository.getNextPrescriptionId()).thenReturn(1L);

        Prescription prescription = buildPrescription();
        Set<PrescriptionItem> prescriptionItems = buildMixedPrescriptionItems();
        Iterator<PrescriptionItem> iterator = prescriptionItems.iterator();
        PrescriptionItem prescriptionItem1 = iterator.next();
        PrescriptionItem prescriptionItem2 = iterator.next();
        prescription.setPrescriptionItems(prescriptionItems);
        prescription.associateChildObjects();

        int mixedPlanId1 = prescriptionItem1.getMedicine().getExchangePlanId().intValue();
        int mixedPlanId2 = prescriptionItem2.getMedicine().getExchangePlanId().intValue();
        GalbopPrescription expectedPrescription1 = galbopService.createPrescription(mixedPlanId1, newNumeration1, prescription, prescriptionItems);
        GalbopPrescription expectedPrescription2 = galbopService.createPrescription(mixedPlanId2, newNumeration2, prescription, prescriptionItems);
        doNothing().when(prescriptionService).save(prescription);

        galbopService.savePrescription(prescription);

        assertPrescription(expectedPrescription1, newNumeration1, prescription, mixedPlanId1);
        assertPrescription(expectedPrescription2, newNumeration2, prescription, mixedPlanId2);
        assertThat(prescription.getExchangeId()).contains(newNumeration1.getNumeration());
        assertThat(prescription.getExchangeId()).contains(newNumeration2.getNumeration());
        verify(galbopRepository, times(2)).savePrescription(any());
        verify(galbopPrescriptionValidator, times(2)).validate(any(GalbopPrescription.class), any(Prescription.class));
    }

    @Test
    public void testGetRegistrationNumberReturnsZeroWhenInvalidRegistrationCode() {
        long result = galbopService.getRegistrationNumber("invalid string");
        assertThat(result).isZero();
    }

    @Test
    public void testGetRegistrationNumberReturnsValidNumberWhenCodeWithZeroPrefix() {
        long result = galbopService.getRegistrationNumber("01234");
        assertThat(result).isEqualTo(1234L);
    }

    @Test
    public void testGetRegistrationNumberReturnsValidNumberWhenCodeWithTextPrefix() {
        long result = galbopService.getRegistrationNumber("MP1234");
        assertThat(result).isEqualTo(1234L);
    }

    @Test
    public void testGetRegistrationNumberReturnsValidNumberWhenCodeWithTextPrefixAndSpaces() {
        long result = galbopService.getRegistrationNumber("MP 0234");
        assertThat(result).isEqualTo(234L);
    }

    @Test
    public void testGetRegistrationNumberReturnsValidNumberWhenCodeWithTextPrefixSufixAndSpaces() {
        long result = galbopService.getRegistrationNumber("MP 0034 abc123");
        assertThat(result).isEqualTo(34L);
    }

    @Test
    public void testSyncPrescriptionsDoNotSetUtilizedWhenNotAllExchangeIdsMatched() {
        Prescription prescription1 = new Prescription();
        prescription1.getExchangeId().add(20191234L);

        Prescription prescription2 = new Prescription();
        prescription2.getExchangeId().add(201912345L);
        prescription2.getExchangeId().add(2019123456L);

        Set<Prescription> prescriptions = new HashSet<>();
        prescriptions.add(prescription1);
        prescriptions.add(prescription2);

        GalbopPrescriptionDetail galbopPrescriptionDetail1 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail1.setPrescriptionNumber("2019123456");

        GalbopPrescriptionDetail galbopPrescriptionDetail2 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail2.setPrescriptionNumber("20191234");

        Set<GalbopPrescriptionDetail> galbopPrescriptionDetails = new HashSet<>();
        galbopPrescriptionDetails.add(galbopPrescriptionDetail1);
        galbopPrescriptionDetails.add(galbopPrescriptionDetail2);

        Status utilized = new Status();
        utilized.setId(StatusReference.PRESCRIPTION_UTILIZED.getId());

        when(galbopRepository.findAllPrescriptionDetails(anyString(), anySet())).thenReturn(galbopPrescriptionDetails);

        galbopService.syncPrescriptionsStatus(prescriptions, utilized);

        assertThat(prescription1.getStatus()).isEqualTo(utilized);
        assertThat(prescription2.getStatus()).isNull();
    }

    @Test
    public void testSyncPrescriptionsSetUtilizedWhenAllExchangeIdsMatched() {
        Prescription prescription1 = new Prescription();
        prescription1.getExchangeId().add(20191234L);

        Prescription prescription2 = new Prescription();
        prescription2.getExchangeId().add(201912345L);
        prescription2.getExchangeId().add(2019123456L);

        Set<Prescription> prescriptions = new HashSet<>();
        prescriptions.add(prescription1);
        prescriptions.add(prescription2);

        GalbopPrescriptionDetail galbopPrescriptionDetail1 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail1.setPrescriptionNumber("201912345");

        GalbopPrescriptionDetail galbopPrescriptionDetail2 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail2.setPrescriptionNumber("20191239");

        GalbopPrescriptionDetail galbopPrescriptionDetail3 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail3.setPrescriptionNumber("2019123456");

        Set<GalbopPrescriptionDetail> galbopPrescriptionDetails = new HashSet<>();
        galbopPrescriptionDetails.add(galbopPrescriptionDetail1);
        galbopPrescriptionDetails.add(galbopPrescriptionDetail2);
        galbopPrescriptionDetails.add(galbopPrescriptionDetail3);

        Status utilized = new Status();
        utilized.setId(StatusReference.PRESCRIPTION_UTILIZED.getId());

        when(galbopRepository.findAllPrescriptionDetails(anyString(), anySet())).thenReturn(galbopPrescriptionDetails);

        galbopService.syncPrescriptionsStatus(prescriptions, utilized);

        assertThat(prescription1.getStatus()).isNull();
        assertThat(prescription2.getStatus()).isEqualTo(utilized);
    }

    @Test
    public void testSyncPrescriptionsDoNotSetUtilizedWhenEmptyExchangeIds() {
        Prescription prescription1 = new Prescription();
        prescription1.getExchangeId().add(20191234L);

        Prescription prescription2 = new Prescription();

        Set<Prescription> prescriptions = new HashSet<>();
        prescriptions.add(prescription1);
        prescriptions.add(prescription2);

        GalbopPrescriptionDetail galbopPrescriptionDetail1 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail1.setPrescriptionNumber("20191234");

        GalbopPrescriptionDetail galbopPrescriptionDetail2 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail2.setPrescriptionNumber("20191239");

        GalbopPrescriptionDetail galbopPrescriptionDetail3 = new GalbopPrescriptionDetail();
        galbopPrescriptionDetail3.setPrescriptionNumber("2019123456");

        Set<GalbopPrescriptionDetail> galbopPrescriptionDetails = new HashSet<>();
        galbopPrescriptionDetails.add(galbopPrescriptionDetail1);
        galbopPrescriptionDetails.add(galbopPrescriptionDetail2);
        galbopPrescriptionDetails.add(galbopPrescriptionDetail3);

        Status utilized = new Status();
        utilized.setId(StatusReference.PRESCRIPTION_UTILIZED.getId());

        when(galbopRepository.findAllPrescriptionDetails(anyString(), anySet())).thenReturn(galbopPrescriptionDetails);

        galbopService.syncPrescriptionsStatus(prescriptions, utilized);

        assertThat(prescription1.getStatus()).isEqualTo(utilized);
        assertThat(prescription2.getStatus()).isNull();
    }

    @Test
    public void testFindBeneficiaryValidatedPrescriptionWhenNoValidResults() {
        String beneficiaryCode = "03-30529599/00";
        String parsedBeneficiaryCode = "3305295990";

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode(beneficiaryCode);

        Optional<Prescription> result = galbopService.findBeneficiaryValidatedPrescription(beneficiary, parsedBeneficiaryCode);

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindBeneficiaryValidatedPrescriptionReturnsEmptyWhenNoProducts() throws ObjectNotValidException {
        String beneficiaryCode = "03-30529599/00";
        String parsedBeneficiaryCode = "3305295990";

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode(beneficiaryCode);

        GalbopPrescriptionNumeration lastNumberation = new GalbopPrescriptionNumeration();
        lastNumberation.setNumeration(2019010201L);

        GalbopPrescriptionNumeration newNumeration = new GalbopPrescriptionNumeration();
        newNumeration.setNumeration(lastNumberation.getNumeration() + 1);

        when(galbopRepository.getNextPrescriptionId()).thenReturn(1L);

        Prescription prescription = buildPrescription();
        PrescriptionItem prescriptionItem = buildSinglePrescriptionItem();
        prescription.getPrescriptionItems().add(prescriptionItem);
        prescription.associateChildObjects();

        int uniqueMedicinePlanId = prescriptionItem.getMedicine().getExchangePlanId().intValue();
        GalbopPrescription galbopPrescription = galbopService.createPrescription(uniqueMedicinePlanId, newNumeration, prescription, prescription.getPrescriptionItems());
        GalbopPrescriptionDetail galbopPrescriptionDetail = galbopPrescription.getPrescriptionDetails().iterator().next();

        when(galbopRepository.findValidatedPrescriptionDetail(parsedBeneficiaryCode, GALBOP_PREAUTHORIZED_PLAN_ID, newNumeration.getNumeration().toString()))
                .thenReturn(Optional.of(galbopPrescriptionDetail));
        when(galbopRepository.findProductsInPrescriptionDetail(galbopPrescriptionDetail)).thenReturn(Collections.emptySet());

        Optional<Prescription> result = galbopService.findBeneficiaryValidatedPrescription(beneficiary, newNumeration.getNumeration().toString());

        assertThat(result).isEmpty();
    }

    @Test
    public void testFindBeneficiaryValidatedPrescriptionReturnsValidPrescriptionWhenResults() throws ObjectNotValidException {
        String beneficiaryCode = "03-30529599/00";
        String parsedBeneficiaryCode = "3305295990";

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode(beneficiaryCode);

        GalbopPrescriptionNumeration lastNumberation = new GalbopPrescriptionNumeration();
        lastNumberation.setNumeration(2019010201L);

        GalbopPrescriptionNumeration newNumeration = new GalbopPrescriptionNumeration();
        newNumeration.setNumeration(lastNumberation.getNumeration() + 1);

        when(galbopRepository.getNextPrescriptionId()).thenReturn(1L);

        Prescription prescription = buildPrescription();
        PrescriptionItem prescriptionItem = buildSinglePrescriptionItem();
        prescription.getPrescriptionItems().add(prescriptionItem);
        prescription.associateChildObjects();
        ProductIntegrationProjection productProjection = buildProductIntegrationProjection(prescriptionItem.getMedicine());

        int uniqueMedicinePlanId = prescriptionItem.getMedicine().getExchangePlanId().intValue();
        GalbopPrescription galbopPrescription = galbopService.createPrescription(uniqueMedicinePlanId, newNumeration, prescription, prescription.getPrescriptionItems());
        GalbopPrescriptionDetail galbopPrescriptionDetail = galbopPrescription.getPrescriptionDetails().iterator().next();
        GalbopPrescriptionLine galbopPrescriptionLine = galbopPrescriptionDetail.getPrescriptionLines().iterator().next();

        when(galbopRepository.findValidatedPrescriptionDetail(parsedBeneficiaryCode, GALBOP_PREAUTHORIZED_PLAN_ID, newNumeration.getNumeration().toString()))
                .thenReturn(Optional.of(galbopPrescriptionDetail));
        when(galbopRepository.findProductsInPrescriptionDetail(galbopPrescriptionDetail)).thenReturn(Collections.singleton(productProjection));

        Optional<Prescription> result = galbopService.findBeneficiaryValidatedPrescription(beneficiary, newNumeration.getNumeration().toString());

        assertThat(result).isPresent();
        Prescription prescriptionResult = result.get();
        assertThat(prescriptionResult.getExchangeId()).contains(newNumeration.getNumeration());
        assertThat(prescriptionResult.getExpirationDate()).isEqualTo(galbopPrescriptionDetail.getDateTo());
        PrescriptionItem prescriptionItemResult = prescriptionResult.getPrescriptionItems().iterator().next();
        assertThat(prescriptionItemResult.getQuantity()).isEqualTo(2);
        assertThat(prescriptionItemResult.getDailyDosage()).isEqualTo(galbopPrescriptionLine.getDailyDosage().toString());
        Medicine medicineResult = prescriptionItemResult.getMedicine();
        assertThat(medicineResult.getExchangePlanId()).isEqualTo(GALBOP_PREAUTHORIZED_PLAN_ID);
        assertThat(medicineResult.getExchangeId()).isEqualTo(productProjection.getExchangeId());
        assertThat(medicineResult.getProduct()).isEqualTo(productProjection.getProduct());
        assertThat(medicineResult.getProductTypeId()).isEqualTo(productProjection.getProductTypeId());
        assertThat(medicineResult.getPresentation()).isEqualTo(productProjection.getPresentation());
        assertThat(medicineResult.getRecommendation()).isEqualTo(productProjection.getRecommendation());
        assertThat(medicineResult.getUnits()).isEqualTo(productProjection.getUnits());
        assertThat(medicineResult.getUnitsTypeId()).isEqualTo(productProjection.getUnitsTypeId());
        assertThat(medicineResult.getConcentration()).isEqualTo(productProjection.getConcentration());
        assertThat(medicineResult.getConcentrationTypeId()).isEqualTo(productProjection.getConcentrationTypeId());
        assertThat(medicineResult.getDrugId()).isEqualTo(productProjection.getDrugId());
        assertThat(medicineResult.getAuthorizedDosage()).isEqualTo(productProjection.getAuthorizedDosage());
    }

    private Prescription buildPrescription() {
        Prescription prescription = new Prescription();
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryCode("03-30529599/00");
        MedicalRegistration medicalRegistration = new MedicalRegistration();
        medicalRegistration.setRegistrationCode("123456");
        Practitioner practitioner = new Practitioner();
        practitioner.getMedicalRegistrations().add(medicalRegistration);
        practitioner.setName("Juan");
        practitioner.setLastName("Perez");

        prescription.setId(1L);
        prescription.setObservations("Observation");
        prescription.setBeneficiary(beneficiary);
        prescription.setPractitioner(practitioner);
        prescription.setExpirationPeriod(Period.WEEKLY);
        return prescription;
    }

    private Set<PrescriptionItem> buildRegularPrescriptionItems() {
        PrescriptionItem prescriptionItem1 = new PrescriptionItem();
        Medicine medicine1 = new Medicine();
        medicine1.setExchangePlanId(1L);
        medicine1.setExchangeId(12L);
        medicine1.setProduct("Product");
        medicine1.setProductTypeId(1L);
        medicine1.setPresentation("xPresentation");
        medicine1.setUnits(20);
        medicine1.setUnitsTypeId(1L);
        medicine1.setDrugId(13L);
        medicine1.setConcentrationTypeId(1L);
        medicine1.setConcentration(new BigDecimal(200));
        medicine1.setAuthorizedDosage(new BigDecimal("2.7"));
        prescriptionItem1.setMedicine(medicine1);
        prescriptionItem1.setQuantity(1);
        prescriptionItem1.setDailyDosage("3,5mg");
        prescriptionItem1.setDisease(null);

        PrescriptionItem prescriptionItem2 = new PrescriptionItem();
        Medicine medicine2 = new Medicine();
        medicine2.setExchangePlanId(1L);
        medicine2.setExchangeId(13L);
        medicine2.setProduct("Product2");
        medicine2.setProductTypeId(2L);
        medicine2.setPresentation("xPresentation2");
        medicine2.setUnits(30);
        medicine2.setUnitsTypeId(2L);
        medicine2.setDrugId(14L);
        medicine2.setConcentrationTypeId(2L);
        medicine2.setConcentration(new BigDecimal(300));
        medicine2.setAuthorizedDosage(new BigDecimal("2.1"));
        ICD10Disease icd10Disease2 = new ICD10Disease();
        icd10Disease2.setCode("A2");
        prescriptionItem2.setMedicine(medicine2);
        prescriptionItem2.setQuantity(2);
        prescriptionItem2.setDailyDosage("1,5mg");
        prescriptionItem2.setDisease(icd10Disease2);

        Set<PrescriptionItem> prescriptionItems = new HashSet<>();
        prescriptionItems.add(prescriptionItem1);
        prescriptionItems.add(prescriptionItem2);
        return prescriptionItems;
    }

    private PrescriptionItem buildSinglePrescriptionItem() {
        PrescriptionItem prescriptionItem = new PrescriptionItem();
        Medicine medicine = new Medicine();
        medicine.setExchangePlanId(1L);
        medicine.setExchangeId(13L);
        medicine.setProduct("Product2");
        medicine.setProductTypeId(2L);
        medicine.setPresentation("xPresentation2");
        medicine.setUnits(30);
        medicine.setUnitsTypeId(2L);
        medicine.setDrugId(14L);
        medicine.setConcentrationTypeId(2L);
        medicine.setConcentration(new BigDecimal(300));
        medicine.setAuthorizedDosage(new BigDecimal("2.1"));
        ICD10Disease icd10Disease2 = new ICD10Disease();
        icd10Disease2.setCode("A2");
        prescriptionItem.setMedicine(medicine);
        prescriptionItem.setQuantity(2);
        prescriptionItem.setDailyDosage("1,5mg");
        prescriptionItem.setDisease(icd10Disease2);
        return prescriptionItem;
    }

    private Set<PrescriptionItem> buildMixedPrescriptionItems() {
        PrescriptionItem prescriptionItem1 = new PrescriptionItem();
        Medicine medicine1 = new Medicine();
        medicine1.setExchangePlanId(1L);
        medicine1.setExchangeId(12L);
        medicine1.setProduct("Product");
        medicine1.setRecommendation("Recco2");
        medicine1.setProductTypeId(1L);
        medicine1.setPresentation("xPresentation");
        medicine1.setUnits(20);
        medicine1.setUnitsTypeId(1L);
        medicine1.setDrugId(13L);
        medicine1.setConcentrationTypeId(1L);
        medicine1.setConcentration(new BigDecimal(200));
        medicine1.setAuthorizedDosage(new BigDecimal("1.5"));
        ICD10Disease icd10Disease1 = new ICD10Disease();
        icd10Disease1.setCode("A1");
        prescriptionItem1.setMedicine(medicine1);
        prescriptionItem1.setQuantity(1);
        prescriptionItem1.setDailyDosage("3,5mg");
        prescriptionItem1.setDisease(icd10Disease1);

        PrescriptionItem prescriptionItem2 = new PrescriptionItem();
        Medicine medicine2 = new Medicine();
        medicine2.setExchangePlanId(2L);
        medicine2.setExchangeId(13L);
        medicine2.setProduct("Product2");
        medicine2.setRecommendation("Recco1");
        medicine2.setProductTypeId(2L);
        medicine2.setPresentation("xPresentation2");
        medicine2.setUnits(30);
        medicine2.setUnitsTypeId(2L);
        medicine2.setDrugId(14L);
        medicine2.setConcentrationTypeId(2L);
        medicine2.setConcentration(new BigDecimal(300));
        medicine2.setAuthorizedDosage(new BigDecimal("1.1"));
        prescriptionItem2.setMedicine(medicine2);
        prescriptionItem2.setQuantity(2);
        prescriptionItem2.setDailyDosage("1,5mg");
        prescriptionItem2.setDisease(null);

        Set<PrescriptionItem> prescriptionItems = new HashSet<>();
        prescriptionItems.add(prescriptionItem1);
        prescriptionItems.add(prescriptionItem2);
        return prescriptionItems;
    }

    private ProductIntegrationProjection buildProductIntegrationProjection(Medicine medicine) {
        ProductIntegrationProjection productIntegrationProjection = mock(ProductIntegrationProjection.class);
        when(productIntegrationProjection.getExchangeId()).thenReturn(medicine.getExchangeId());
        when(productIntegrationProjection.getProduct()).thenReturn(medicine.getProduct());
        when(productIntegrationProjection.getProductTypeId()).thenReturn(medicine.getProductTypeId());
        when(productIntegrationProjection.getPresentation()).thenReturn(medicine.getPresentation());
        when(productIntegrationProjection.getRecommendation()).thenReturn(medicine.getRecommendation());
        when(productIntegrationProjection.getUnits()).thenReturn(medicine.getUnits());
        when(productIntegrationProjection.getUnitsTypeId()).thenReturn(medicine.getUnitsTypeId());
        when(productIntegrationProjection.getConcentration()).thenReturn(medicine.getConcentration());
        when(productIntegrationProjection.getConcentrationTypeId()).thenReturn(medicine.getConcentrationTypeId());
        when(productIntegrationProjection.getDrugId()).thenReturn(medicine.getDrugId());
        when(productIntegrationProjection.getAuthorizedDosage()).thenReturn(medicine.getAuthorizedDosage());
        return productIntegrationProjection;
    }

    private void assertPrescription(GalbopPrescription expectedPrescription, GalbopPrescriptionNumeration newNumeration, Prescription prescription, int planId) {
        Practitioner practitioner = prescription.getPractitioner();
        Set<PrescriptionItem> prescriptionItems = prescription.getPrescriptionItems();

        assertThat(expectedPrescription.getId().getPrescriptionId()).isEqualTo(1L);
        assertThat(expectedPrescription.getFunderId()).isEqualTo(2050);
        assertThat(expectedPrescription.getId().getPlanId()).isEqualTo(planId);
        assertThat(expectedPrescription.getId().getBeneficiaryTypeId()).isEqualTo(GALBOP_DEFAULT_BENEFICIARY_TYPE);
        assertThat(expectedPrescription.getId().getBeneficiaryCode()).isEqualTo("3305295990");
        LocalDateTime resolvedPeriod = DateUtils.resolvePeriodDateTo(prescription.getExpirationPeriod());
        assertThat(expectedPrescription.getDateTo()).isEqualTo(resolvedPeriod.toLocalDate());
        assertThat(expectedPrescription.getApplicationFormId()).isEqualTo(StringUtils.join(GALBOP_PRESCRIPTION_FORM_PREFIX, SLASH, prescription.getId()));
        assertThat(expectedPrescription.getStatus()).isNull();
        assertThat(expectedPrescription.getRegistrationType()).isEqualTo(GALBOP_DEFAULT_REGISTRATION_TYPE);
        assertThat(expectedPrescription.getRegistration()).isEqualTo(Long.valueOf(practitioner.getMedicalRegistrations().iterator().next().getRegistrationCode()));
        assertThat(expectedPrescription.getPractitioner()).isEqualTo(StringUtils.join(PRACTITIONER_NAME_PREFIX, WHITESPACE, practitioner.getLastName().toUpperCase(), COMA, WHITESPACE, practitioner.getName().toUpperCase()));
        assertThat(expectedPrescription.getPractitionerType()).isEqualTo(GALBOP_DEFAULT_PRACTITIONER_TYPE);
        assertThat(expectedPrescription.getRegistrationProvince()).isEqualTo(GALBOP_DEFAULT_REGISTRATION_PROVINCE);
        assertThat(expectedPrescription.getDisease()).isNotEmpty();
        assertThat(expectedPrescription.getObservations()).isNotEmpty();
        assertThat(expectedPrescription.getOrigin()).isEqualTo(GALBOP_DEFAULT_ORIGIN);

        GalbopPrescriptionDetail expectedPrescriptionDetail = expectedPrescription.getPrescriptionDetails().iterator().next();

        assertThat(expectedPrescriptionDetail.getId().getPrescriptionId()).isEqualTo(expectedPrescription.getId().getPrescriptionId());
        assertThat(expectedPrescriptionDetail.getId().getPrescriptionValue()).isEqualTo(1);
        assertThat(expectedPrescriptionDetail.getStatus()).isEmpty();
        assertThat(expectedPrescriptionDetail.getPrescriptionNumber()).isEqualTo(String.valueOf(newNumeration.getNumeration()));
        assertThat(expectedPrescriptionDetail.getAuthorizationId()).isZero();
        assertThat(expectedPrescriptionDetail.getDateFrom()).isEqualTo(expectedPrescription.getDateFrom());
        assertThat(expectedPrescriptionDetail.getDateTo()).isEqualTo(expectedPrescription.getDateTo());
        assertThat(expectedPrescriptionDetail.getPrescriptionLines().size()).isEqualTo(prescription.getPrescriptionItems().size());

        expectedPrescriptionDetail.getPrescriptionLines().forEach(l -> {
            PrescriptionItem item = prescriptionItems.stream().filter((i -> i.getMedicine().getExchangeId().equals(l.getProductId()))).findAny().get();
            Medicine medicine = item.getMedicine();

            assertThat(l.getId().getPrescriptionId()).isEqualTo(expectedPrescription.getId().getPrescriptionId());
            assertThat(l.getId().getPrescriptionValue()).isEqualTo(expectedPrescriptionDetail.getId().getPrescriptionValue());
            assertThat(l.getId().getOrder()).satisfiesAnyOf((i -> assertThat(i).isEqualTo(1)), (i -> assertThat(i).isEqualTo(2)));
            Long dosage = Long.valueOf(medicine.getUnits());

            assertThat(l.getDosage()).isEqualTo(dosage);
            assertThat(l.getAuthorizedDosage()).isEqualTo(medicine.getAuthorizedDosage().multiply(BigDecimal.valueOf(item.getQuantity())));
            assertThat(l.getRecommendation()).isEqualTo(StringUtils.join(medicine.getRecommendation(), WHITESPACE, medicine.getPresentation()));
            assertThat(l.getProductId()).isEqualTo(medicine.getExchangeId());

            GalbopPrescriptionValidation expectedValidation = l.getPrescriptionValidations().iterator().next();
            assertThat(expectedValidation.getId().getPrescriptionId()).isEqualTo(expectedPrescription.getId().getPrescriptionId());
            assertThat(expectedValidation.getId().getPrescriptionValue()).isEqualTo(expectedPrescriptionDetail.getId().getPrescriptionValue());
            assertThat(expectedValidation.getId().getOrder()).satisfiesAnyOf((i -> assertThat(i).isEqualTo(1)), (i -> assertThat(i).isEqualTo(2)));
            assertThat(expectedValidation.getId().getSubOrder()).isEqualTo(1);
            assertThat(expectedValidation.getDrugId()).isEqualTo(medicine.getDrugId());
            assertThat(expectedValidation.getProductTypeId()).isEqualTo(medicine.getProductTypeId());
            assertThat(expectedValidation.getConcentration().toString()).hasToString(medicine.getConcentration().toString());
            assertThat(expectedValidation.getConcentrationTypeId()).isEqualTo(medicine.getConcentrationTypeId());
            assertThat(expectedValidation.getUnitsTypeId()).isEqualTo(medicine.getUnitsTypeId());
            assertThat(expectedValidation.getUnits().toString()).hasToString(medicine.getUnits().toString());
        });
    }

}
