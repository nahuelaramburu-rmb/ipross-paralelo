package com.capacidad.validationapi.module.prescription.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.DateUtils;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Period;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.prescription.dto.PrescriptionDTO;
import com.capacidad.validationapi.module.prescription.model.Prescription;
import com.capacidad.validationapi.module.prescription.projection.PrescriptionProjection;
import com.capacidad.validationapi.module.prescription.repository.PrescriptionRepository;
import com.capacidad.validationapi.module.prescription.service.PrescriptionIntegrationInvoker;
import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
import com.capacidad.validationapi.module.prescription.service.PrescriptionSupportService;
import com.capacidad.validationapi.module.prescription.service.PrescriptionValidator;
import com.capacidad.validationapi.prescription.integration.DefaultPrescriptionServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.validationapi.functional.BiThrowingConsumer.biThrowingConsumer;
import static com.capacidad.validationapi.functional.ThrowingConsumer.throwingConsumer;
import static com.capacidad.validationapi.module.general.reference.StatusReference.*;

@Log4j2
@Service
@Transactional(value = "chainedTransactionManager", rollbackFor = Exception.class)
public class PrescriptionServiceImpl extends BaseServiceImpl<Prescription, PrescriptionDTO, Long> implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionValidator prescriptionValidator;
    private final BeneficiaryFinder beneficiaryFinder;
    private final PrescriptionSupportService prescriptionSupportService;
    private final PrescriptionIntegrationInvoker prescriptionIntegrationInvoker;

    @Autowired
    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository,
                                   PrescriptionValidator prescriptionValidator,
                                   BeneficiaryFinder beneficiaryFinder,
                                   PrescriptionSupportService prescriptionSupportService,
                                   PrescriptionIntegrationInvoker prescriptionIntegrationInvoker) {
        super(prescriptionRepository);
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionValidator = prescriptionValidator;
        this.beneficiaryFinder = beneficiaryFinder;
        this.prescriptionSupportService = prescriptionSupportService;
        this.prescriptionIntegrationInvoker = prescriptionIntegrationInvoker;
    }

    @Override
    public Prescription create(PrescriptionDTO dto) throws ObjectNotValidException, ObjectNotFoundException {
        Prescription prescription = this.mapDtoToInput(dto);
        validate(prescription);
        initialize(prescription);
        Prescription result = prescriptionRepository.save(prescription);
        prescriptionIntegrationInvoker.invokeCreation(result);
        prescriptionSupportService.sendNewPrescriptionNotification(prescription);
        return result;
    }

    @Override
    public void validate(Prescription prescription) throws ObjectNotFoundException, ObjectNotValidException {
        prescriptionValidator.validate(prescription);
    }

    private void initialize(Prescription prescription) throws ObjectNotValidException {
        Status status = this.getUtils().getEntityReference(Status.class, PRESCRIPTION_APPROVED.getId());
        prescription.setStatus(status);
        Period expirationPeriod = prescriptionSupportService.getPrescriptionExpirationPeriod();
        prescription.setDType(StringUtils.uncapitalize(DefaultPrescriptionServiceImpl.class.getSimpleName()));
        prescription.setExpirationPeriod(expirationPeriod);
        prescription.setExpirationDate(DateUtils.resolvePeriodDateTo(expirationPeriod).toLocalDate());
        prescription.setKey(prescriptionSupportService.buildPrescriptionKey(prescription));
        prescription.getPrescriptionItems().forEach(p -> p.setDisease(this.getUtils().getEntityReference(ICD10Disease.class, p.getDisease().getId())));
        prescription.associateChildObjects();
    }

    @Override
    public void createFromMedicalAuthorization(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException, ObjectNotFoundException {
        Set<Prescription> prescriptions = medicalAuthorization.getPrescriptions();
        if (!prescriptions.isEmpty()) {
            for (Prescription prescription : prescriptions) {
                prescription.setBeneficiary(medicalAuthorization.getBeneficiary());
                prescription.setPractitioner(medicalAuthorization.getPractitioner());
                prescription.setMedicalCenter(medicalAuthorization.getMedicalCenter());
                prescriptionValidator.validateFromMedicalAuthorization(prescription);
                initialize(prescription);
            }
            Collection<Prescription> prescriptionsResults = prescriptionRepository.saveAll(prescriptions);
            prescriptionIntegrationInvoker.invokeCreation(prescriptionsResults);
        }
    }

    @Override
    public void createAll(Set<PrescriptionDTO> prescriptionDtos) throws ObjectNotValidException, ObjectNotFoundException {
        Set<Prescription> prescriptions = new HashSet<>();
        prescriptionDtos.forEach(throwingConsumer(dto -> {
            Prescription prescription = this.mapDtoToInput(dto);
            validate(prescription);
            initialize(prescription);
            prescriptions.add(prescription);
        }));
        Collection<Prescription> prescriptionsResults = prescriptionRepository.saveAll(prescriptions);
        prescriptionIntegrationInvoker.invokeCreation(prescriptionsResults);
        prescriptionsResults.forEach(prescriptionSupportService::sendNewPrescriptionNotification);
    }

    @Override
    public boolean existsByAuthMedicalCenter(long prescriptionId) {
        return prescriptionRepository.existsByIdAndMedicalCenterResourceId(prescriptionId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public boolean existsByAuthPractitioner(long prescriptionId) {
        return prescriptionRepository.existsByIdAndPractitionerResourceId(prescriptionId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public void save(Prescription prescription) {
        prescriptionRepository.saveAndFlush(prescription);
    }

    @Override
    public void saveAll(Collection<Prescription> prescriptions) {
        prescriptionRepository.saveAll(prescriptions);
    }

    @Override
    public PrescriptionProjection cancelPrescription(long prescriptionId, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        Prescription prescription = this.findById(prescriptionId);
        if (prescription.getStatus().getId().equals(PRESCRIPTION_UTILIZED.getId()))
            throw new ObjectNotValidException("prescription.alreadyUtilized", prescription.getId().toString());
        if (prescription.getStatus().getId().equals(StatusReference.PRESCRIPTION_CANCELLED.getId()))
            throw new ObjectNotValidException("prescription.alreadyCancelled", prescription.getId().toString());
        if (Boolean.TRUE.equals(prescription.getPreAuthorized()))
            cancelPreauthorizedPrescription(prescription);
        else
            prescriptionIntegrationInvoker.invokeCancellation(prescription);
        Status cancelled = this.getUtils().getEntityReference(Status.class, StatusReference.PRESCRIPTION_CANCELLED.getId());
        prescription.setStatus(cancelled);
        prescription.setCancellationReason(input.getCancellationReason());
        Prescription saved = prescriptionRepository.save(prescription);
        return this.getProjectionFactory().createProjection(PrescriptionProjection.class, saved);
    }

    private void cancelPreauthorizedPrescription(Prescription prescription) {
        prescription.getExchangeId().clear();
    }

    @Override
    public void syncStatus() {
        Set<Prescription> approvedPrescriptions = prescriptionRepository.findAllByStatusId(PRESCRIPTION_APPROVED.getId());
        Map<String, List<Prescription>> groupedPrescriptions = approvedPrescriptions.stream().collect(Collectors.groupingBy(Prescription::getDType));
        Status utilized = this.getUtils().getEntityReference(Status.class, StatusReference.PRESCRIPTION_UTILIZED.getId());
        groupedPrescriptions.forEach(biThrowingConsumer((k, v) -> syncStatus(k, v, utilized)));
        Status expired = this.getUtils().getEntityReference(Status.class, PRESCRIPTION_EXPIRED.getId());
        approvedPrescriptions.forEach(p -> {
            if (p.getStatus().getId().equals(PRESCRIPTION_APPROVED.getId()) && LocalDate.now().isAfter(p.getExpirationDate()))
                p.setStatus(expired);
        });
        prescriptionRepository.saveAll(approvedPrescriptions);
    }

    private void syncStatus(String prescriptionServiceName, Collection<Prescription> prescriptions, Status newStatus) throws ObjectNotValidException {
        prescriptionIntegrationInvoker.invokeStatusSynchronization(prescriptionServiceName, prescriptions, newStatus);
    }

    @Override
    public ByteArrayOutputStream generateReceipt(long prescriptionId) throws ObjectNotFoundException, ObjectNotValidException {
        Prescription prescription = this.findById(prescriptionId);
        return prescriptionSupportService.generatePrescriptionReceipt(prescription);
    }

    @Override
    public PrescriptionProjection.Integration findPrescription(String beneficiaryCode, Long exchangeId) throws ObjectNotFoundException, ObjectNotValidException {
        Beneficiary beneficiary = beneficiaryFinder.findBeneficiary(beneficiaryCode);
        Prescription prescription = prescriptionIntegrationInvoker.invokeFindPrescription(beneficiary, exchangeId.toString())
                .orElseThrow(() -> new ObjectNotFoundException("prescription.integrationNotFound"));
        return this.getProjectionFactory().createProjection(PrescriptionProjection.Integration.class, prescription);
    }

    @Override
    public boolean existsByAuthBeneficiaryOrRelative(long prescriptionId) {
        boolean exists = prescriptionRepository.existsByIdAndBeneficiaryResourceId(prescriptionId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
        if (exists)
            return true;
        return prescriptionRepository.existsByIdAndBeneficiaryFamilyId(prescriptionId, beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
    }

    @Override
    public Optional<Specification<Prescription>> appendCustomSpecification() {
        if (SecurityUtils.isBeneficiary()) {
            return Optional.of((root, query, builder) -> {
                var join = root.join("beneficiary");
                return builder.equal(join.get("familyId"), beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
            });
        }
        if (SecurityUtils.isPractitioner() || SecurityUtils.isMedicalCenter())
            return Optional.of((root, query, builder) -> {
                String entity = SecurityUtils.isPractitioner() ? "practitioner" : "medicalCenter";
                var join = root.join(entity);
                return builder.equal(join.get("resourceId"), SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
            });
        return Optional.empty();
    }

}
