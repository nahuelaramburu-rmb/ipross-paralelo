package com.capacidad.validationapi.module.medicalauthorization.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.disease.model.ICD10Disease;
import com.capacidad.validationapi.module.general.model.Status;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.medicalauthorization.dto.CancellationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationDTO;
import com.capacidad.validationapi.module.medicalauthorization.dto.MedicalAuthorizationDiagnosisDTO;
import com.capacidad.validationapi.module.medicalauthorization.model.AuthorizationType;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.medicalauthorization.projection.MedicalAuthorizationProjection;
import com.capacidad.validationapi.module.medicalauthorization.repository.MedicalAuthorizationRepository;
import com.capacidad.validationapi.module.medicalauthorization.repository.RestrictionTypeRepository;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationRoleSpecificationBuilder;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationService;
import com.capacidad.validationapi.module.medicalauthorization.service.MedicalAuthorizationSupportService;
import com.capacidad.validationapi.module.medicalcenter.model.MedicalCenter;
import com.capacidad.validationapi.module.practitioner.service.PractitionerService;
import com.capacidad.validationapi.module.procedure.dto.MessageDTO;
import com.capacidad.validationapi.module.procedure.model.Message;
import com.capacidad.validationapi.module.rating.Rating;
import com.capacidad.validationapi.module.rating.RatingDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_CANCELLED;
import static com.capacidad.validationapi.module.general.reference.StatusReference.VALIDATION_REJECTED;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class MedicalAuthorizationServiceImpl extends BaseServiceImpl<MedicalAuthorization, MedicalAuthorizationDTO, Long> implements MedicalAuthorizationService {

    private final MedicalAuthorizationRepository medicalAuthorizationRepository;
    private final BeneficiaryFinder beneficiaryFinder;
    private final RestrictionTypeRepository restrictionTypeRepository;
    private final ContractMediator contractMediator;
    private final MedicalAuthorizationSupportService medicalAuthorizationSupportService;
    private final MedicalAuthorizationRoleSpecificationBuilder roleSpecificationBuilder;
    private final PractitionerService practitionerService;


    @Autowired
    public MedicalAuthorizationServiceImpl(MedicalAuthorizationRepository repository,
                                           BeneficiaryFinder beneficiaryFinder,
                                           RestrictionTypeRepository restrictionTypeRepository,
                                           ContractMediator contractMediator,
                                           MedicalAuthorizationSupportService medicalAuthorizationSupportService,
                                           MedicalAuthorizationRoleSpecificationBuilder roleSpecificationBuilder,
                                           PractitionerService practitionerService) {
        super(repository);
        this.medicalAuthorizationRepository = repository;
        this.beneficiaryFinder = beneficiaryFinder;
        this.restrictionTypeRepository = restrictionTypeRepository;
        this.contractMediator = contractMediator;
        this.medicalAuthorizationSupportService = medicalAuthorizationSupportService;
        this.roleSpecificationBuilder = roleSpecificationBuilder;
        this.practitionerService = practitionerService;
    }

    @Override
    public boolean existsByAuthMedicalCenter(long validationId) {
        return medicalAuthorizationRepository.existsByIdAndMedicalCenterResourceId(validationId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public boolean existsByAuthPractitioner(long validationId) {
        return medicalAuthorizationRepository.existsByIdAndPractitionerResourceId(validationId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
    }

    @Override
    public boolean existsByAuthOrganization(long authorizationId) throws ObjectNotFoundException {
        Set<Contract> organizationContracts = contractMediator.findAllAuthOrganizationAndRelatedContracts();
        return medicalAuthorizationRepository.existsByIdAndContractIn
                (authorizationId, organizationContracts);
    }

    @Override
    public boolean existsByAuthBeneficiaryOrRelative(long authorizationId) {
        return medicalAuthorizationRepository.existsByIdAndBeneficiaryResourceId(authorizationId, SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                || medicalAuthorizationRepository.existsByIdAndBeneficiaryFamilyId(authorizationId, beneficiaryFinder.findOptionallyAuthBeneficiaryFamilyId().orElse(null));
    }

    @Override
    public int getMedicalCenterAuthorizationTypeAmountInPeriod(MedicalAuthorization medicalAuthorization, AuthorizationType authorizationType, LocalDateTime from) {
        MedicalCenter medicalCenter = medicalAuthorization.getMedicalCenter();
        List<MedicalAuthorization> items = medicalAuthorizationRepository
                .findAllByMedicalCenterAndAuthorizationTypeIdAndCreatedAtBetweenAndStatusIdNotInAndIdIsNot
                        (medicalCenter, authorizationType.getId(), from, LocalDateTime.now(), getIgnoreStatuses(), medicalAuthorization.getId());
        return items.size();
    }

    private Set<Long> getIgnoreStatuses() {
        Set<Long> statusIds = new HashSet<>();
        statusIds.add(StatusReference.VALIDATION_REJECTED.getId());
        statusIds.add(StatusReference.VALIDATION_CANCELLED.getId());
        statusIds.add(StatusReference.VALIDATION_PENDING.getId());
        return statusIds;
    }

    @Override
    public MedicalAuthorizationProjection cancelMedicalAuthorization(long medicalAuthorizationId, CancellationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = this.findById(medicalAuthorizationId);
        if (medicalAuthorization.getStatus().getId().equals(VALIDATION_CANCELLED.getId()))
            throw new ObjectNotValidException("medicalAuthorization.alreadyCancelled", medicalAuthorization.getId().toString());
        if (medicalAuthorization.getStatus().getId().equals(VALIDATION_REJECTED.getId()))
            throw new ObjectNotValidException("medicalAuthorization.rejected", medicalAuthorization.getId().toString());
        Status cancelled = this.getUtils().getGenericsEntityReference(Status.class, VALIDATION_CANCELLED.getId());
        medicalAuthorizationSupportService.rollBackPreMedicalAuthorization(medicalAuthorization);
        medicalAuthorization.getMedicalAuthorizationItems().forEach(medicalAuthorizationItem -> medicalAuthorizationItem.setStatus(cancelled));
        medicalAuthorization.setStatus(cancelled);
        medicalAuthorization.setCancellationReason(input.getCancellationReason());
        medicalAuthorization.setChargeTotal(new BigDecimal(0));
        MedicalAuthorization updatedMedicalAuthorization = medicalAuthorizationRepository.save(medicalAuthorization);
        medicalAuthorizationSupportService.discountChargesAndValues(medicalAuthorization);
        practitionerService.removeRating(medicalAuthorization.getPractitioner(), medicalAuthorization.getRating());
        medicalAuthorizationSupportService.publishStatusUpdateEventAndNotifyAuditors(medicalAuthorization);
        return this.getProjectionFactory().createProjection(MedicalAuthorizationProjection.class, updatedMedicalAuthorization);
    }

    @Override
    public MedicalAuthorizationProjection.Diagnosis updateAuthorizationDiagnosis(long medicalAuthorizationId, MedicalAuthorizationDiagnosisDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = this.findById(medicalAuthorizationId);
        boolean isValid = medicalAuthorization.isPending() || medicalAuthorization.isPartiallyApproved() || medicalAuthorization.isApproved();
        if (!isValid)
            throw new ObjectNotValidException("medicalAuthorization.notPending", medicalAuthorization.getId().toString());
        medicalAuthorization.setDisease(null);
        if (input.getDisease() != null) {
            Long diseaseId = input.getDisease().getId();
            ICD10Disease disease = this.getUtils().getGenericsEntityReference(ICD10Disease.class, diseaseId);
            medicalAuthorization.setDisease(disease);
        }
        medicalAuthorization.setDiagnosis(input.getDiagnosis());
        MedicalAuthorization updatedMedicalAuthorization = medicalAuthorizationRepository.save(medicalAuthorization);
        medicalAuthorizationSupportService.publishDiagnosisUpdateEventAndNotifyAuditors(medicalAuthorization);
        return this.getProjectionFactory().createProjection(MedicalAuthorizationProjection.Diagnosis.class, updatedMedicalAuthorization);
    }

    @Override
    public long getMedicalAuthorizationItemParentId(long medicalAuthorizationItemId) throws ObjectNotValidException {
        BaseProjection<Long> result = medicalAuthorizationRepository.findByMedicalAuthorizationItemsId(medicalAuthorizationItemId)
                .orElseThrow(() -> new ObjectNotValidException(
                        "medicalAuthorizationItem.notFound",
                        String.valueOf(medicalAuthorizationItemId)));
        return result.getId();
    }

    @Override
    public ByteArrayOutputStream generateReceipt(long medicalAuthorizationId) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = this.findById(medicalAuthorizationId);
        return medicalAuthorizationSupportService.buildReceipt(medicalAuthorization);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllRestrictionTypes() {
        return restrictionTypeRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public MedicalAuthorizationProjection addRating(long medicalAuthorizationId, RatingDTO ratingDTO) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = this.findById(medicalAuthorizationId);
        if (!medicalAuthorization.isApproved())
            throw new ObjectNotValidException("medicalAuthorization.rateApprovedOnly");
        if (medicalAuthorization.getRating() != null)
            throw new ObjectNotValidException("medicalAuthorization.alreadyRated");
        Rating rating = buildRating(ratingDTO);
        medicalAuthorization.setRating(rating);
        MedicalAuthorization updated = medicalAuthorizationRepository.save(medicalAuthorization);
        practitionerService.addRating(medicalAuthorization.getPractitioner(), rating);
        return this.getProjectionFactory().createProjection(MedicalAuthorizationProjection.class, updated);
    }

    @Override
    public Set<Message> receiveMessage(long medicalAuthorizationId, MessageDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        MedicalAuthorization medicalAuthorization = this.findById(medicalAuthorizationId);

        if (!medicalAuthorization.isPending())
            throw new ObjectNotValidException("medicalAuthorization.notPending", medicalAuthorization.getId().toString());

        String sender = SecurityUtils.getAuthenticatedAuthorityPrincipal().orElse("");
        LocalDateTime time = LocalDateTime.now();
        String text = input.getText();

        Message message = new Message();
        message.setFrom(sender);
        message.setSentAt(time);
        message.setText(text);

        Set<Message> messages = medicalAuthorization.getMessages();
        messages.add(message);

        medicalAuthorization.setMessages(messages);
        medicalAuthorizationRepository.save(medicalAuthorization);

        medicalAuthorizationSupportService.publishNewMessageEventAndNotifyAuditors(medicalAuthorization);

        return messages;
    }

    @Override
    public Set<Message> dumpAllMessages(long medicalAuthorizationId) throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = this.findById(medicalAuthorizationId);
        return medicalAuthorization.getMessages();
    }

    @Override
    public MedicalAuthorizationProjection.Diagnosis getDiagnosis(long medicalAuthorizationId) throws ObjectNotFoundException {
        MedicalAuthorization medicalAuthorization = this.findById(medicalAuthorizationId);
        return this.getProjectionFactory().createProjection(MedicalAuthorizationProjection.Diagnosis.class, medicalAuthorization);
    }

    private Rating buildRating(RatingDTO ratingDTO) {
        Rating rating = new Rating();
        rating.setQuality(new BigDecimal(ratingDTO.getQuality()));
        rating.setCharges(new BigDecimal(ratingDTO.getCharges()));
        rating.setDuration(new BigDecimal(ratingDTO.getDuration()));
        rating.setWaitTime(new BigDecimal(ratingDTO.getWaitTime()));
        rating.setAverage(calculateRatingAverage(rating));
        rating.setQuantity(1);
        return rating;
    }

    private BigDecimal calculateRatingAverage(Rating rating) {
        return rating.getQuality().add(rating.getDuration()).add(rating.getWaitTime()).divide(new BigDecimal(3), 1, RoundingMode.HALF_UP);
    }

    @Override
    public Optional<Specification<MedicalAuthorization>> appendCustomSpecification() {
        return roleSpecificationBuilder.buildSpecification();
    }

}
