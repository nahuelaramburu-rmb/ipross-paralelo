package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.service.BaseServiceFinderImpl;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryVerificationDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeCompanyReport;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeMonthlyReport;
import com.capacidad.validationapi.module.beneficiary.repository.BeneficiaryRepository;
import com.capacidad.validationapi.module.beneficiary.repository.PaymentMethodRepository;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.general.reference.StatusReference;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import com.capacidad.validationapi.module.person.repository.RelationshipTypeRepository;
import com.capacidad.validationapi.module.properties.service.PropertiesService;
import com.capacidad.validationapi.module.tradeunion.service.TradeUnionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.capacidad.validationapi.misc.constant.ModelConstants.CREATED_AT;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_CHARGES;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_COMPANY_CHARGES;

@Log4j2
@Service
public class BeneficiaryFinderServiceImpl extends BaseServiceFinderImpl<Beneficiary, Long> implements BeneficiaryFinder {

    private final BeneficiaryRepository beneficiaryRepository;
    private final PropertiesService propertiesService;
    private final RelationshipTypeRepository relationshipTypeRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TradeUnionService tradeUnionService;

    @Autowired
    public BeneficiaryFinderServiceImpl(BeneficiaryRepository beneficiaryRepository,
                                        PropertiesService propertiesService,
                                        RelationshipTypeRepository relationshipTypeRepository,
                                        PaymentMethodRepository paymentMethodRepository,
                                        TradeUnionService tradeUnionService) {
        super(beneficiaryRepository);
        this.beneficiaryRepository = beneficiaryRepository;
        this.propertiesService = propertiesService;
        this.relationshipTypeRepository = relationshipTypeRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.tradeUnionService = tradeUnionService;
    }

    @Override
    public Beneficiary findBeneficiaryLocked(long idNumber, IdType idType) throws ObjectNotFoundException {
        return beneficiaryRepository
                .findLockedByIdNumberAndIdTypeId(idNumber, idType.getId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "beneficiary.notFoundIdNumberIdType",
                        idType.getId().toString(), String.valueOf(idNumber))
                );
    }

    @Override
    public Beneficiary findBeneficiary(String beneficiaryCode) throws ObjectNotFoundException {
        return beneficiaryRepository
                .findByBeneficiaryCode(beneficiaryCode)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "beneficiary.notFoundBeneficiaryCode",
                        String.valueOf(beneficiaryCode))
                );
    }

    @Override
    public Beneficiary findBeneficiaryLocked(String beneficiaryCode) throws ObjectNotFoundException {
        return beneficiaryRepository
                .findLockedByBeneficiaryCode(beneficiaryCode)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "beneficiary.notFoundBeneficiaryCode",
                        String.valueOf(beneficiaryCode))
                );
    }

    @Override
    public Beneficiary findByIdLocked(long beneficiaryId) throws ObjectNotFoundException {
        return beneficiaryRepository.findLockedById(beneficiaryId)
                .orElseThrow(() -> new ObjectNotFoundException("base.notFound", String.valueOf(beneficiaryId)));
    }

    @Override
    public Beneficiary findAuthBeneficiary() throws ObjectNotFoundException {
        return beneficiaryRepository
                .findByResourceId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException("beneficiary.notFoundResourceId"));
    }

    @Override
    public BeneficiaryProjection findBeneficiaryProjected(long idNumber, IdType idType) throws ObjectNotFoundException {
        return beneficiaryRepository
                .findProjectedByIdNumberAndIdTypeId(idNumber, idType.getId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "beneficiary.notFoundIdNumberIdType",
                        idType.getId().toString(), String.valueOf(idNumber))
                );
    }

    @Override
    public BeneficiaryProjection findBeneficiaryProjected(String beneficiaryCode) throws ObjectNotFoundException {
        return beneficiaryRepository
                .findProjectedByBeneficiaryCode(beneficiaryCode)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "beneficiary.notFoundBeneficiaryCode",
                        String.valueOf(beneficiaryCode))
                );
    }

    @Override
    public PersonDetailProjection findBeneficiaryPersonDetailProjected(long beneficiaryId) throws ObjectNotFoundException {
        return beneficiaryRepository
                .findBeneficiaryProjectedById(beneficiaryId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "beneficiary.notFound",
                        String.valueOf(beneficiaryId))
                );
    }

    @Override
    public BeneficiaryProjection findAuthBeneficiaryProjected() throws ObjectNotFoundException {
        return beneficiaryRepository
                .findProjectedByResourceId(SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null))
                .orElseThrow(() -> new ObjectNotFoundException("beneficiary.notFoundResourceId"));
    }

    @Override
    public BeneficiaryProjection.Verification verifyBeneficiary(BeneficiaryVerificationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        List<BeneficiaryProjection.Verification> results = beneficiaryRepository
                .findAllProjectedByIdNumberAndIdTypeIdAndBirthDateAndBeneficiaryCode
                        (input.getIdNumber(), input.getIdType().getId(), input.getBirthDate(), input.getBeneficiaryCode());
        if (results.isEmpty())
            throw new ObjectNotFoundException(
                    "beneficiary.notFoundBeneficiaryCodeBirthDateIdNumberIdType",
                    input.getBeneficiaryCode(), String.valueOf(input.getIdNumber()), input.getBirthDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        BeneficiaryProjection.Verification projection = results.get(0);
        validateVerificationAge(projection.getAge());
        if (!projection.hasActiveHealthCoverage())
            throw new ObjectNotValidException("beneficiary.noCoverage", input.getBeneficiaryCode());
        return projection;
    }

    private void validateVerificationAge(int age) throws ObjectNotValidException {
        if (age < propertiesService.getProperties().getBeneficiaryMinAccountAge())
            throw new ObjectNotValidException("beneficiary.underAge", String.valueOf(age));
    }

    @Override
    public List<BeneficiaryProjection> getRelatives(long beneficiaryId) throws ObjectNotFoundException {
        Beneficiary beneficiary = this.findById(beneficiaryId);
        return beneficiaryRepository.findAllProjectedByFamilyIdAndIdIsNot(beneficiary.getFamilyId(), beneficiaryId, Sort.by(CREATED_AT).ascending());
    }

    @Override
    public Set<Beneficiary> getFamily(UUID familyId) {
        return beneficiaryRepository.findAllByFamilyId(familyId);
    }

    @Override
    public Optional<UUID> findOptionallyAuthBeneficiaryFamilyId() {
        try {
            if (SecurityUtils.isBeneficiary()) {
                return Optional.of(this.findAuthBeneficiary().getFamilyId());
            }
        } catch (ObjectNotFoundException e) {
            log.error("getAuthBeneficiaryFamilyId - Auth Beneficiary not found: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<IdAndNameOnlyProjection> findAllRelationshipTypes() {
        return relationshipTypeRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public List<IdAndNameOnlyProjection> getAllPaymentMethods() {
        return paymentMethodRepository.findAllProjectedBy(IdAndNameOnlyProjection.class);
    }

    @Override
    public PageModelWrapper<BeneficiaryChargeMonthlyReport> getAllChargesGroupedByMonth(long beneficiaryId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        Page<BeneficiaryChargeMonthlyReport> page = beneficiaryRepository.sumChargeTotalsGroupedByBeneficiary(beneficiaryId, StatusReference.VALIDATION_APPROVED.getId(), pageRequest);
        return new PageModelWrapper<>(RESOURCE_CHARGES, page, pageable);
    }

    @Override
    public PageModelWrapper<BeneficiaryChargeCompanyReport> getAllChargesGroupedByCompany(long beneficiaryId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        Page<BeneficiaryChargeCompanyReport> page = beneficiaryRepository.sumChargeTotalsGroupedByBeneficiaryAndCompany(beneficiaryId, StatusReference.VALIDATION_APPROVED.getId(), pageRequest);
        return new PageModelWrapper<>(RESOURCE_COMPANY_CHARGES, page, pageable);
    }

    @Override
    public boolean correspondsToAuthentication(long beneficiaryId) {
        UUID resourceId = SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null);
        boolean exists = beneficiaryRepository
                .existsByIdAndResourceId(beneficiaryId, resourceId);
        if (!exists) {
            try {
                Beneficiary beneficiary = this.findById(beneficiaryId);
                exists = beneficiaryRepository
                        .existsByResourceIdAndFamilyId(resourceId, beneficiary.getFamilyId());
            } catch (ObjectNotFoundException ex) {
                return false;
            }
        }
        return exists;
    }

    @Override
    public Set<IdAndNameOnlyProjection> findAllTradeUnions(long beneficiaryId) {
        return tradeUnionService.findAllByBeneficiaryId(beneficiaryId);
    }

}
