package com.capacidad.validationapi.prescription.galbop.repository;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescription;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionDetail;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionLine;
import com.capacidad.validationapi.prescription.galbop.model.GalbopPrescriptionNumeration;
import com.capacidad.validationapi.prescription.galbop.projection.GalbopBeneficiaryProjection;
import com.capacidad.validationapi.prescription.galbop.projection.GalbopPrescriptionIdProjection;
import com.capacidad.validationapi.prescription.integration.ProductIntegrationProjection;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Profile({"!test"})
@Service
@Log4j2
@Transactional(value = "chainedTransactionManager", rollbackFor = Exception.class)
public class GalbopRepositoryImpl implements GalbopRepository {

    private final GalbopProductRepository galbopProductRepository;
    private final GalbopPrescriptionRepository galbopPrescriptionRepository;
    private final GalbopPrescriptionDetailRepository galbopPrescriptionDetailRepository;
    private final GalbopPrescriptionNumerationRepository galbopPrescriptionNumerationRepository;
    private final GalbopBeneficiaryRepository galbopBeneficiaryRepository;

    @Autowired
    public GalbopRepositoryImpl(GalbopProductRepository productRepository,
                                GalbopPrescriptionRepository galbopPrescriptionRepository,
                                GalbopPrescriptionDetailRepository galbopPrescriptionDetailRepository,
                                GalbopPrescriptionNumerationRepository galbopPrescriptionNumerationRepository,
                                GalbopBeneficiaryRepository galbopBeneficiaryRepository) {
        this.galbopProductRepository = productRepository;
        this.galbopPrescriptionRepository = galbopPrescriptionRepository;
        this.galbopPrescriptionDetailRepository = galbopPrescriptionDetailRepository;
        this.galbopPrescriptionNumerationRepository = galbopPrescriptionNumerationRepository;
        this.galbopBeneficiaryRepository = galbopBeneficiaryRepository;

    }

    @Override
    public List<GalbopBeneficiaryProjection> findBeneficiaryGroupedPlans(String beneficiaryCode) {
        return galbopBeneficiaryRepository.findGalbopBeneficiaryPlans(beneficiaryCode);
    }

    @Override
    public GalbopPrescriptionNumeration getLastNumeration() throws ObjectNotValidException {
        try {
            return galbopPrescriptionNumerationRepository.findTopByOrderByNumerationDesc();
        } catch (Exception e) {
            log.error("{} - getLastNumeration: {}", this.getClass(), e.getMessage());
            throw new ObjectNotValidException("prescription.integrationError");
        }
    }

    @Override
    public void updateNumeration(GalbopPrescriptionNumeration newNumeration) throws ObjectNotValidException {
        try {
            galbopPrescriptionNumerationRepository.updateNumeration(newNumeration.getNumeration());
        } catch (Exception e) {
            log.error("{} - updateNumeration: {}", this.getClass(), e.getMessage());
            throw new ObjectNotValidException("prescription.integrationError");
        }
    }

    @Override
    public long getNextPrescriptionId() throws ObjectNotValidException {
        try {
            GalbopPrescriptionIdProjection id = galbopPrescriptionRepository.findTopByOrderByPrescriptionIdDesc();
            return id.getPrescriptionId() + 1;
        } catch (Exception e) {
            log.error("{} - getNextPrescriptionId: {}", this.getClass(), e.getMessage());
            throw new ObjectNotValidException("prescription.integrationError");
        }
    }

    @Override
    public GalbopPrescription savePrescription(GalbopPrescription galbopPrescription) throws ObjectNotValidException {
        try {
            return galbopPrescriptionRepository.save(galbopPrescription);
        } catch (Exception e) {
            log.error("{} - savePrescription: {}", this.getClass(), e.getMessage());
            throw new ObjectNotValidException("prescription.integrationError");
        }
    }

    @Override
    public GalbopPrescriptionDetail savePrescriptionDetail(GalbopPrescriptionDetail galbopPrescriptionDetail) throws ObjectNotValidException {
        try {
            return galbopPrescriptionDetailRepository.save(galbopPrescriptionDetail);
        } catch (Exception e) {
            log.error("{} - savePrescriptionDetail: {}", this.getClass(), e.getMessage());
            throw new ObjectNotValidException("prescription.integrationError");
        }
    }

    @Override
    public Set<ProductIntegrationProjection> findProductsByNameAndVademecumIds(String name, int[] vademecumIds) {
        return galbopProductRepository
                .findAllByNameAndVademecumId(StringUtils.join("%", name, "%"), vademecumIds);
    }

    @Override
    public Set<ProductIntegrationProjection> findProductsInPrescriptionDetail(GalbopPrescriptionDetail galbopPrescriptionDetail) {
        Set<Long> productIds = galbopPrescriptionDetail.getPrescriptionLines().stream()
                .map(GalbopPrescriptionLine::getProductId)
                .collect(Collectors.toUnmodifiableSet());
        return galbopProductRepository.findAllByProductIdIn(productIds);
    }

    @Override
    public void updatePrescriptionDetail(Set<String> prescriptionNumbers, String status, LocalDateTime statusDate) {
        galbopPrescriptionDetailRepository.updatePrescriptionDetail(prescriptionNumbers, status, statusDate);
    }

    @Override
    public Set<GalbopPrescriptionDetail> findAllPrescriptionDetails(String status, Set<String> prescriptionNumbers) {
        return galbopPrescriptionDetailRepository.findAllByStatusAndPrescriptionNumberIn(status, prescriptionNumbers);
    }

    @Override
    public Optional<GalbopPrescriptionDetail> findValidatedPrescriptionDetail(String beneficiaryCode, Long planId, String prescriptionNumber) {
        return galbopPrescriptionDetailRepository
                .findValidatedPrescriptionDetail
                        (prescriptionNumber, LocalDate.now(), planId, beneficiaryCode);

    }

}
