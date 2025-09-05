package com.capacidad.validationapi.module.beneficiary.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.hateoas.PageModelWrapper;
import com.capacidad.validationapi.module.base.service.BaseServiceFinder;
import com.capacidad.validationapi.module.beneficiary.dto.BeneficiaryVerificationDTO;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.projection.BeneficiaryProjection;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeCompanyReport;
import com.capacidad.validationapi.module.beneficiary.report.BeneficiaryChargeMonthlyReport;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.person.model.IdType;
import com.capacidad.validationapi.module.person.projection.PersonDetailProjection;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BeneficiaryFinder extends BaseServiceFinder<Beneficiary, Long> {

    Beneficiary findBeneficiaryLocked(long idNumber, IdType idType) throws ObjectNotFoundException;

    Beneficiary findBeneficiary(String beneficiaryCode) throws ObjectNotFoundException;

    Beneficiary findBeneficiaryLocked(String beneficiaryCode) throws ObjectNotFoundException;

    Beneficiary findByIdLocked(long beneficiaryId) throws ObjectNotFoundException;

    Beneficiary findAuthBeneficiary() throws ObjectNotFoundException;

    BeneficiaryProjection findBeneficiaryProjected(long idNumber, IdType idType) throws ObjectNotFoundException;

    BeneficiaryProjection findBeneficiaryProjected(String beneficiaryCode) throws ObjectNotFoundException;

    PersonDetailProjection findBeneficiaryPersonDetailProjected(long beneficiaryId) throws ObjectNotFoundException;

    BeneficiaryProjection findAuthBeneficiaryProjected() throws ObjectNotFoundException;

    BeneficiaryProjection.Verification verifyBeneficiary(BeneficiaryVerificationDTO input) throws ObjectNotFoundException, ObjectNotValidException;

    List<BeneficiaryProjection> getRelatives(long beneficiaryId) throws ObjectNotFoundException;

    Set<Beneficiary> getFamily(UUID familyId);

    Optional<UUID> findOptionallyAuthBeneficiaryFamilyId();

    List<IdAndNameOnlyProjection> findAllRelationshipTypes();

    List<IdAndNameOnlyProjection> getAllPaymentMethods();

    PageModelWrapper<BeneficiaryChargeMonthlyReport> getAllChargesGroupedByMonth(long beneficiaryId, Pageable pageable);

    PageModelWrapper<BeneficiaryChargeCompanyReport> getAllChargesGroupedByCompany(long beneficiaryId, Pageable pageable);

    boolean correspondsToAuthentication(long beneficiaryId);

    Set<IdAndNameOnlyProjection> findAllTradeUnions(long beneficiaryId);

}
