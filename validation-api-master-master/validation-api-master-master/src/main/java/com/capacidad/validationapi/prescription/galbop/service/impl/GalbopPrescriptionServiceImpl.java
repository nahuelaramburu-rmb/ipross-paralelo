package com.capacidad.validationapi.prescription.galbop.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.medicine.model.Medicine;
import com.capacidad.validationapi.module.practitioner.model.Practitioner;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.model.PrescriptionItem;
import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
import com.capacidad.validationapi.prescription.galbop.model.*;
import com.capacidad.validationapi.prescription.galbop.projection.GalbopBeneficiaryProjection;
import com.capacidad.validationapi.prescription.galbop.repository.GalbopRepository;
import com.capacidad.validationapi.prescription.galbop.service.GalbopPrescriptionValidator;
import com.capacidad.validationapi.prescription.integration.MedicinesIntegration;
import com.capacidad.validationapi.prescription.integration.PrescriptionIntegration;
import com.capacidad.validationapi.prescription.integration.ProductIntegrationProjection;
import com.capacidad.validationapi.prescription.integration.ProductWrapperDTO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;

@Profile({"!test"})
@Log4j2
@Service
@Transactional(value = "chainedTransactionManager", rollbackFor = Exception.class)
public class GalbopPrescriptionServiceImpl implements PrescriptionIntegration, MedicinesIntegration {

    protected static final String GALBOP_PLAN_MAPPING_PREFIX = "PLAN_BEHAVIOUR_";
    protected static final String GALBOP_PRESCRIPTION_FORM_PREFIX = "VEM";
    protected static final String GALBOP_DEFAULT_REGISTRATION_TYPE = "P";
    protected static final String GALBOP_DEFAULT_PRACTITIONER_TYPE = "MD";
    protected static final String GALBOP_DEFAULT_BENEFICIARY_TYPE = "0";
    protected static final String GALBOP_DEFAULT_REGISTRATION_PROVINCE = "R";
    protected static final String GALBOP_DEFAULT_ORIGIN = "RECETA WEB";
    protected static final String PRACTITIONER_NAME_PREFIX = "DR.";
    protected static final Long GALBOP_PREAUTHORIZED_PLAN_ID = 101L;
    protected static final Long GALBOP_DEFAULT_FUNDER_ID = 2050L;
    private final GalbopRepository galbopRepository;
    private final PrescriptionService prescriptionService;
    private final GalbopPrescriptionValidator galbopPrescriptionValidator;

    @Autowired
    public GalbopPrescriptionServiceImpl(GalbopRepository galbopRepository,
                                         PrescriptionService prescriptionService,
                                         GalbopPrescriptionValidator galbopPrescriptionValidator) {
        this.galbopRepository = galbopRepository;
        this.prescriptionService = prescriptionService;
        this.galbopPrescriptionValidator = galbopPrescriptionValidator;
    }

    @Override
    public void savePrescription(Prescription prescription) throws ObjectNotFoundException, ObjectNotValidException {
        if (prescription.getExchangeId().isEmpty()) {
            Map<Long, List<PrescriptionItem>> prescriptionMap = determineMultiplePlans(prescription);
            savePrescription(prescriptionMap);
            prescriptionService.save(prescription);
        } else
            updatePreAuthorizedPrescription(prescription);
    }

    private Map<Long, List<PrescriptionItem>> determineMultiplePlans(Prescription prescription) {
        return prescription.getPrescriptionItems().stream().collect(Collectors.groupingBy(o -> o.getMedicine().getExchangePlanId()));
    }

    private void savePrescription(Map<Long, List<PrescriptionItem>> prescriptionMap) throws ObjectNotValidException, ObjectNotFoundException {
        for (Map.Entry<Long, List<PrescriptionItem>> item : prescriptionMap.entrySet()) {
            Prescription prescription = item.getValue().get(0).getPrescription();
            GalbopPrescriptionNumeration numeration = updatePrescriptionNumeration();
            GalbopPrescription galbopPrescription = createPrescription(item.getKey(), numeration, prescription, item.getValue());
            galbopPrescriptionValidator.validate(galbopPrescription, prescription);
            galbopRepository.savePrescription(galbopPrescription);
            prescription.getExchangeId().add(numeration.getNumeration());
        }
    }

    protected GalbopPrescription createPrescription(long planId, GalbopPrescriptionNumeration numeration, Prescription prescription, Collection<PrescriptionItem> prescriptionItems) throws ObjectNotValidException {
        GalbopPrescription galbopPrescription = initPrescription(planId, prescription);
        GalbopPrescriptionDetail galbopPrescriptionDetail = createPrescriptionDetail(galbopPrescription, numeration);
        galbopPrescription.getPrescriptionDetails().add(galbopPrescriptionDetail);
        Set<GalbopPrescriptionLine> prescriptionLines = createPrescriptionLines(galbopPrescriptionDetail, prescriptionItems);
        galbopPrescriptionDetail.getPrescriptionLines().addAll(prescriptionLines);
        return galbopPrescription;
    }

    private GalbopPrescriptionNumeration updatePrescriptionNumeration() throws ObjectNotValidException {
        GalbopPrescriptionNumeration prescriptionNumeration = galbopRepository.getLastNumeration();
        GalbopPrescriptionNumeration newNumberation = new GalbopPrescriptionNumeration();
        newNumberation.setNumeration(prescriptionNumeration.getNumeration() + 1);
        galbopRepository.updateNumeration(newNumberation);
        return newNumberation;
    }

    private GalbopPrescription initPrescription(long planId, Prescription prescription) throws ObjectNotValidException {
        GalbopPrescription galbopPrescription = new GalbopPrescription();
        long prescriptionNextId = galbopRepository.getNextPrescriptionId();
        String beneficiaryCode = parseBeneficiaryCode(prescription.getBeneficiary().getBeneficiaryCode());
        GalbopPrescriptionEmbeddedId embeddedId = new GalbopPrescriptionEmbeddedId(prescriptionNextId,
                planId,
                GALBOP_DEFAULT_BENEFICIARY_TYPE,
                beneficiaryCode);
        galbopPrescription.setId(embeddedId);
        galbopPrescription.setFunderId(GALBOP_DEFAULT_FUNDER_ID);
        galbopPrescription.setDateFrom(LocalDate.now());
        LocalDateTime resolvedPeriod = DateUtils.resolvePeriodDateTo(prescription.getExpirationPeriod());
        galbopPrescription.setDateTo(resolvedPeriod.toLocalDate());
        galbopPrescription.setCreatedAtDate(LocalDate.now());
        galbopPrescription.setCreatedAtTime(LocalTime.now());
        galbopPrescription.setApplicationFormId(StringUtils.join(GALBOP_PRESCRIPTION_FORM_PREFIX, SLASH, prescription.getId()));
        galbopPrescription.setStatus(null);
        galbopPrescription.setRegistrationType(GALBOP_DEFAULT_REGISTRATION_TYPE);
        String registration = prescription.getPractitioner().getMedicalRegistrations().iterator().next().getRegistrationCode();
        galbopPrescription.setRegistration(getRegistrationNumber(registration));
        galbopPrescription.setPractitioner(buildPractitionerName(prescription.getPractitioner()));
        galbopPrescription.setPractitionerType(GALBOP_DEFAULT_PRACTITIONER_TYPE);
        galbopPrescription.setRegistrationProvince(GALBOP_DEFAULT_REGISTRATION_PROVINCE);
        galbopPrescription.setOrigin(GALBOP_DEFAULT_ORIGIN);
        galbopPrescription.setDisease(buildDisease(prescription.getPrescriptionItems()));
        galbopPrescription.setObservations(prescription.getObservations().getBytes(StandardCharsets.UTF_8));
        return galbopPrescription;
    }

    protected String parseBeneficiaryCode(String beneficiaryCode) {
        Pattern pattern = Pattern.compile("-([0-9]+)/");
        Matcher matcher = pattern.matcher(beneficiaryCode);
        if (matcher.find()) {
            String rawIdNumber = matcher.group(1);
            String idNumber = StringUtils.stripStart(rawIdNumber, "0");
            String[] typeDivided = StringUtils.split(beneficiaryCode, SLASH);
            String[] leadingDivided = StringUtils.split(beneficiaryCode, DASH);
            String leading = leadingDivided[0].charAt(0) == '0' ? StringUtils.substring(leadingDivided[0], 1) : leadingDivided[0];
            String type = typeDivided[1].charAt(0) == '0' ? StringUtils.substring(typeDivided[1], 1) : typeDivided[1];
            return StringUtils.join(leading, idNumber, type);
        }
        return "";
    }

    protected long getRegistrationNumber(String registrationValue) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(registrationValue);
        if (matcher.find())
            return Long.parseLong(matcher.group(0));
        return 0;
    }

    private String buildPractitionerName(Practitioner practitioner) {
        return StringUtils.join(PRACTITIONER_NAME_PREFIX,
                WHITESPACE,
                practitioner.getLastName().toUpperCase(),
                COMA,
                WHITESPACE,
                practitioner.getName().toUpperCase());
    }

    private String buildDisease(Set<PrescriptionItem> prescriptionItems) {
        return prescriptionItems.stream()
                .filter(p -> p.getDisease() != null)
                .map(p -> p.getDisease().getCode())
                .collect(Collectors.joining(COMA));
    }

    private GalbopPrescriptionDetail createPrescriptionDetail(GalbopPrescription galbopPrescription, GalbopPrescriptionNumeration galbopPrescriptionNumeration) {
        GalbopPrescriptionDetail galbopPrescriptionDetail = new GalbopPrescriptionDetail();
        GalbopPrescriptionDetailEmbeddedId embeddedId = new GalbopPrescriptionDetailEmbeddedId(galbopPrescription.getId().getPrescriptionId(), 1);
        galbopPrescriptionDetail.setId(embeddedId);
        galbopPrescriptionDetail.setStatus("");
        galbopPrescriptionDetail.setPrescriptionNumber(String.valueOf(galbopPrescriptionNumeration.getNumeration()));
        galbopPrescriptionDetail.setAuthorizationId(0L);
        galbopPrescriptionDetail.setDateFrom(galbopPrescription.getDateFrom());
        galbopPrescriptionDetail.setDateTo(galbopPrescription.getDateTo());
        return galbopPrescriptionDetail;
    }

    private Set<GalbopPrescriptionLine> createPrescriptionLines(GalbopPrescriptionDetail galbopPrescriptionDetail, Collection<PrescriptionItem> prescriptionItems) {
        Set<GalbopPrescriptionLine> prescriptionLines = new HashSet<>();
        int order = 1;
        for (PrescriptionItem prescriptionItem : prescriptionItems) {
            GalbopPrescriptionLine prescriptionLine = buildPrescriptionLine(galbopPrescriptionDetail, prescriptionItem, order);
            prescriptionLines.add(prescriptionLine);
            order++;
        }
        return prescriptionLines;
    }

    private GalbopPrescriptionLine buildPrescriptionLine(GalbopPrescriptionDetail galbopPrescriptionDetail, PrescriptionItem prescriptionItem, int order) {
        GalbopPrescriptionLine galbopPrescriptionLine = new GalbopPrescriptionLine();
        GalbopPrescriptionLineEmbeddedId embeddedId = new GalbopPrescriptionLineEmbeddedId(galbopPrescriptionDetail.getId(), order);
        galbopPrescriptionLine.setId(embeddedId);
        galbopPrescriptionLine.setDailyDosage(1L);
        Medicine medicine = prescriptionItem.getMedicine();
        galbopPrescriptionLine.setDosage(Long.valueOf(medicine.getUnits()));
        galbopPrescriptionLine.setAuthorizedDosage(medicine.getAuthorizedDosage().multiply(BigDecimal.valueOf(prescriptionItem.getQuantity())));
        galbopPrescriptionLine.setRecommendation(buildRecommendation(medicine));
        galbopPrescriptionLine.setProductId(medicine.getExchangeId());
        GalbopPrescriptionValidation prescriptionValidation = buildPrescriptionValidation(prescriptionItem, galbopPrescriptionLine);
        galbopPrescriptionLine.getPrescriptionValidations().add(prescriptionValidation);
        return galbopPrescriptionLine;
    }

    private String buildRecommendation(Medicine medicine) {
        return StringUtils.join(medicine.getRecommendation(), WHITESPACE, medicine.getPresentation());
    }

    private GalbopPrescriptionValidation buildPrescriptionValidation(PrescriptionItem prescriptionItem, GalbopPrescriptionLine prescriptionLine) {
        GalbopPrescriptionValidation galbopPrescriptionValidation = new GalbopPrescriptionValidation();
        GalbopPrescriptionValidationEmbeddedId embeddedId = new GalbopPrescriptionValidationEmbeddedId(prescriptionLine.getId(), 1);
        galbopPrescriptionValidation.setId(embeddedId);
        Medicine medicine = prescriptionItem.getMedicine();
        galbopPrescriptionValidation.setDrugId(medicine.getDrugId());
        galbopPrescriptionValidation.setProductTypeId(medicine.getProductTypeId());
        galbopPrescriptionValidation.setConcentration(medicine.getConcentration());
        galbopPrescriptionValidation.setConcentrationTypeId(medicine.getConcentrationTypeId());
        galbopPrescriptionValidation.setUnitsTypeId(medicine.getUnitsTypeId());
        galbopPrescriptionValidation.setUnits(Long.valueOf(medicine.getUnits()));
        return galbopPrescriptionValidation;
    }

    private void updatePreAuthorizedPrescription(Prescription prescription) throws ObjectNotFoundException, ObjectNotValidException {
        String parsedBeneficiaryCode = parseBeneficiaryCode(prescription.getBeneficiary().getBeneficiaryCode());
        String prescriptionNumber = prescription.getExchangeId().iterator().next().toString();
        GalbopPrescriptionDetail galbopPrescriptionDetail = galbopRepository
                .findValidatedPrescriptionDetail
                        (parsedBeneficiaryCode, GALBOP_PREAUTHORIZED_PLAN_ID, prescriptionNumber)
                .orElseThrow(() -> new ObjectNotFoundException("prescription.integrationNotFound"));
        prescription.setExpirationDate(galbopPrescriptionDetail.getDateTo());
        prescription.setPreAuthorized(true);
        GalbopPrescription galbopPrescription = galbopPrescriptionDetail.getPrescription();
        String registration = prescription.getPractitioner().getMedicalRegistrations().iterator().next().getRegistrationCode();
        galbopPrescription.setRegistration(getRegistrationNumber(registration));
        galbopPrescription.setPractitioner(buildPractitionerName(prescription.getPractitioner()));
        galbopPrescription.setPractitionerType(GALBOP_DEFAULT_PRACTITIONER_TYPE);
        galbopPrescription.setRegistrationProvince(GALBOP_DEFAULT_REGISTRATION_PROVINCE);
        galbopPrescription.setDisease(buildDisease(prescription.getPrescriptionItems()));
        galbopRepository.savePrescriptionDetail(galbopPrescriptionDetail);
        prescriptionService.save(prescription);
    }

    @Override
    public void savePrescriptions(Collection<Prescription> prescriptions) throws ObjectNotValidException, ObjectNotFoundException {
        for (Prescription prescription : prescriptions) {
            if (prescription.getExchangeId().isEmpty()) {
                Map<Long, List<PrescriptionItem>> prescriptionMap = determineMultiplePlans(prescription);
                savePrescription(prescriptionMap);
                prescriptionService.saveAll(prescriptions);
            } else
                updatePreAuthorizedPrescription(prescription);
        }
    }

    @Override
    public void cancelPrescription(Prescription prescription) {
        Set<String> parsedPrescriptionNumbers = prescription.getExchangeId().stream()
                .map(Object::toString)
                .collect(Collectors.toUnmodifiableSet());
        galbopRepository.updatePrescriptionDetail(parsedPrescriptionNumbers, "X", LocalDateTime.now());
    }

    @Override
    public void syncPrescriptionsStatus(Collection<Prescription> prescriptions, Status newStatus) {
        Set<String> exchangeIds = prescriptions.stream().flatMap(p -> p.getExchangeId().stream().map(Object::toString)).collect(Collectors.toUnmodifiableSet());
        Set<GalbopPrescriptionDetail> galbopPrescriptionDetails = galbopRepository.findAllPrescriptionDetails("A", exchangeIds);
        Set<Long> galbopPrescriptionNumbers = galbopPrescriptionDetails.stream().map(g -> Long.valueOf(g.getPrescriptionNumber())).collect(Collectors.toUnmodifiableSet());
        for (Prescription p : prescriptions) {
            if (!p.getExchangeId().isEmpty() && galbopPrescriptionNumbers.containsAll(p.getExchangeId()))
                p.setStatus(newStatus);
        }
    }

    @Override
    public Optional<Prescription> findBeneficiaryValidatedPrescription(Beneficiary beneficiary, String id) {
        String parsedBeneficiaryCode = parseBeneficiaryCode(beneficiary.getBeneficiaryCode());
        Optional<GalbopPrescriptionDetail> optionalGalbopPrescriptionDetail = galbopRepository.findValidatedPrescriptionDetail
                (parsedBeneficiaryCode, GALBOP_PREAUTHORIZED_PLAN_ID, id);
        if (optionalGalbopPrescriptionDetail.isPresent()) {
            GalbopPrescriptionDetail galbopPrescriptionDetail = optionalGalbopPrescriptionDetail.get();
            Prescription prescription = new Prescription();
            prescription.setExpirationDate(galbopPrescriptionDetail.getDateTo());
            prescription.getExchangeId().add(Long.valueOf(id));
            Set<ProductIntegrationProjection> products = galbopRepository.findProductsInPrescriptionDetail(galbopPrescriptionDetail);
            if (products.isEmpty())
                return Optional.empty();
            Set<PrescriptionItem> prescriptionItems = products.stream()
                    .map(p -> {
                        Medicine medicine = new Medicine();
                        medicine.setExchangePlanId(GALBOP_PREAUTHORIZED_PLAN_ID);
                        medicine.setExchangeId(p.getExchangeId());
                        medicine.setProduct(p.getProduct());
                        medicine.setProductTypeId(p.getProductTypeId());
                        medicine.setPresentation(p.getPresentation());
                        medicine.setRecommendation(p.getRecommendation());
                        medicine.setUnits(p.getUnits());
                        medicine.setUnitsTypeId(p.getUnitsTypeId());
                        medicine.setConcentration(p.getConcentration());
                        medicine.setConcentrationTypeId(p.getConcentrationTypeId());
                        medicine.setDrugId(p.getDrugId());
                        medicine.setAuthorizedDosage(p.getAuthorizedDosage());
                        PrescriptionItem prescriptionItem = new PrescriptionItem();
                        prescriptionItem.setPrescription(prescription);
                        prescriptionItem.setMedicine(medicine);
                        Optional<GalbopPrescriptionLine> optGalbopPrescriptionLine = galbopPrescriptionDetail.getPrescriptionLines().stream()
                                .filter(l -> l.getProductId().equals(p.getExchangeId()))
                                .findFirst();
                        if (optGalbopPrescriptionLine.isPresent()) {
                            GalbopPrescriptionLine galbopPrescriptionLine = optGalbopPrescriptionLine.get();
                            prescriptionItem.setQuantity(galbopPrescriptionLine.getAuthorizedDosage()
                                    .divide(medicine.getAuthorizedDosage(), 2, RoundingMode.HALF_UP)
                                    .intValue());
                            prescriptionItem.setDailyDosage(galbopPrescriptionLine.getDailyDosage().toString());
                        }
                        return prescriptionItem;
                    })
                    .collect(Collectors.toUnmodifiableSet());
            prescription.setPrescriptionItems(prescriptionItems);
            return Optional.of(prescription);
        }
        return Optional.empty();
    }

    @Override
    public ProductWrapperDTO getProducts(String name, String beneficiaryCode) {
        String parsedBeneficiaryCode = parseBeneficiaryCode(beneficiaryCode);
        List<GalbopBeneficiaryProjection> results = galbopRepository
                .findBeneficiaryGroupedPlans(parsedBeneficiaryCode);
        for (GalbopBeneficiaryProjection projection : results) {
            GalbopPlanMapping planMapping = resolveGalbopPlanMapping(projection.getPlanBehaviour());
            Set<ProductIntegrationProjection> productResults = galbopRepository
                    .findProductsByNameAndVademecumIds(StringUtils.join(PERCENT_SIGN, name, PERCENT_SIGN), planMapping.getVademecumIds());
            if (!productResults.isEmpty())
                return new ProductWrapperDTO(productResults, projection.getPlanId());
        }
        return new ProductWrapperDTO(Collections.emptySet(), 0);
    }

    private GalbopPlanMapping resolveGalbopPlanMapping(int planBehaviour) {
        try {
            return GalbopPlanMapping.valueOf(StringUtils.join(GALBOP_PLAN_MAPPING_PREFIX, planBehaviour));
        } catch (IllegalArgumentException e) {
            return GalbopPlanMapping.DEFAULT;
        }
    }

}
