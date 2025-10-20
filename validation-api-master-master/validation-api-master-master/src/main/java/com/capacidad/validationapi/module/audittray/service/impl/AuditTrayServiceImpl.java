package com.capacidad.validationapi.module.audittray.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.misc.MQNameBuilder;
import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.amqp.AmqpService;
import com.capacidad.validationapi.module.audittray.dto.AuditTrayDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.model.Auditor;
import com.capacidad.validationapi.module.audittray.projection.AuditTrayProjection;
import com.capacidad.validationapi.module.audittray.projection.AuditorProjection;
import com.capacidad.validationapi.module.audittray.repository.AuditTrayRepository;
import com.capacidad.validationapi.module.audittray.service.AuditTrayService;
import com.capacidad.validationapi.module.audittray.service.AuditorService;
import com.capacidad.validationapi.module.base.service.BaseServiceImpl;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.location.model.Region;
import com.capacidad.validationapi.module.location.service.RegionService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import com.capacidad.validationapi.module.nomenclator.service.NomenclatorService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.capacidad.validationapi.functional.ThrowingSupplier.throwingSupplier;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.*;
import static com.capacidad.validationapi.module.amqp.AmqpServiceImpl.QUEUE;
import static org.springframework.amqp.core.ExchangeTypes.TOPIC;

@Log4j2
@Service
@Transactional(rollbackFor = Exception.class)
public class AuditTrayServiceImpl extends BaseServiceImpl<AuditTray, AuditTrayDTO, Long> implements AuditTrayService {

    private static final String AUDIT_TRAYS = "audit-trays";
    private static final String AUDITOR_CONNECTIONS = "auditor.connections";
    private final NomenclatorService nomenclatorService;
    private final RegionService regionService;
    private final AuditTrayRepository auditTrayRepository;
    private final AuditorService auditorService;
    private final AmqpService amqpService;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public AuditTrayServiceImpl(AuditTrayRepository repository,
                                NomenclatorService nomenclatorService,
                                RegionService regionService,
                                AuditorService auditorService,
                                AmqpService amqpService,
                                ApplicationProperties applicationProperties) {
        super(repository);
        this.auditTrayRepository = repository;
        this.nomenclatorService = nomenclatorService;
        this.regionService = regionService;
        this.auditorService = auditorService;
        this.amqpService = amqpService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public AuditTray create(AuditTrayDTO dto) throws ObjectNotValidException, ObjectNotFoundException {
        log.info("create - args: {}({})", dto.getClass(), dto);
        AuditTray object = this.mapDtoToInput(dto);
        object.setId(null);
        this.validate(object);
        object.associateChildObjects();
        AuditTray objectResult = auditTrayRepository.save(object);
        createAuditTrayQueue(objectResult);
        return objectResult;
    }

    private void createAuditTrayQueue(AuditTray auditTray) {
        String auditTrayResourceId = auditTray.getResourceId().toString();
        amqpService.createQueue(buildAuditTrayOnlineQueueName(auditTrayResourceId, false));
    }

    private void deleteAuditTrayQueue(AuditTray auditTray) {
        String auditTrayResourceId = auditTray.getResourceId().toString();
        amqpService.deleteQueue(buildAuditTrayOnlineQueueName(auditTrayResourceId, false));
    }

    @Override
    public JsonNode delete(Long objectId) throws ObjectNotFoundException {
        AuditTray objectDb = this.findById(objectId);
        auditTrayRepository.delete(objectDb);
        deleteAuditTrayQueue(objectDb);
        log.info("hardDeleteById - void: deleted {}({})", objectDb.getClass(), objectDb);
        return this.buildIdResponse(objectId);
    }

    @Override
    public void validate(AuditTray auditTray) throws ObjectNotValidException, ObjectNotFoundException {
        validateCityOrRegion(auditTray);
        if (auditTray.getRegion() != null)
            validateRegion(auditTray);
        if (auditTray.getCity() != null)
            validateCity(auditTray);
        auditTray.getAuditors().forEach(auditor -> auditor.setId(getOrCreateAuditor(auditor).getId()));
    }

    private void validateCityOrRegion(AuditTray auditTray) throws ObjectNotValidException {
        if ((auditTray.getCity() != null) == (auditTray.getRegion() != null))
            throw new ObjectNotValidException("auditTray.regionOrCityRequired");
    }

    private void validateRegion(AuditTray auditTray) throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Region region = this.regionService.findById(auditTray.getRegion().getId());
        if (auditTrayRepository.existsByCityOrRegion(auditTray.getNomenclators(), region.getCities()))
            throw new ObjectAlreadyExistsException("auditTray.nomenclatorsAlreadyInUseInRegion");
    }

    private void validateCity(AuditTray auditTray) throws ObjectAlreadyExistsException {
        if (auditTrayRepository
                .existsByCityOrRegion(auditTray.getNomenclators(), Collections.singleton(auditTray.getCity())))
            throw new ObjectAlreadyExistsException("auditTray.nomenclatorsAlreadyInUseInCity");
    }

    @Override
    public void validateUpdate(AuditTray auditTray) throws ObjectNotValidException, ObjectNotFoundException {
        validateCityOrRegion(auditTray);
        if (auditTray.getRegion() != null)
            validateRegionUpdate(auditTray);
        if (auditTray.getCity() != null)
            validateCityUpdate(auditTray);
        auditTray.getAuditors().forEach(auditor -> auditor.setId(getOrCreateAuditor(auditor).getId()));
    }

    private void validateRegionUpdate(AuditTray auditTray) throws ObjectNotFoundException, ObjectAlreadyExistsException {
        Region region = this.regionService.findById(auditTray.getRegion().getId());
        if (auditTrayRepository.existsByCityOrRegionAndIdIsNot(auditTray.getNomenclators(), region.getCities(), auditTray.getId()))
            throw new ObjectAlreadyExistsException("auditTray.nomenclatorsAlreadyInUseInRegion");
    }

    private void validateCityUpdate(AuditTray auditTray) throws ObjectAlreadyExistsException {
        if (auditTrayRepository
                .existsByCityOrRegionAndIdIsNot(auditTray.getNomenclators(),
                        Collections.singleton(auditTray.getCity()),
                        auditTray.getId()))
            throw new ObjectAlreadyExistsException("auditTray.nomenclatorsAlreadyInUseInCity");
    }

    private Auditor getOrCreateAuditor(Auditor auditor) {
        return auditorService
                .findOptionallyBySub(auditor.getSub())
                .orElseGet(throwingSupplier(() -> auditorService.create(auditor)));
    }

    @Override
    public Page<NomenclatorProjection.Minor> getAuditTrayNomenclators(long auditTrayId, Pageable pageable) {
        return nomenclatorService.getAuditTrayNomenclators(auditTrayId, pageable);
    }

    @Override
    public Page<AuditorProjection> getAuditTrayAuditors(long auditTrayId, Pageable pageable) {
        return auditorService.getAuditTrayAuditors(auditTrayId, pageable);
    }

    @Override
    public AuditTrayProjection.Extended addNomenclator(long auditTrayId, long nomenclatorId) throws ObjectNotFoundException, ObjectAlreadyExistsException {
        AuditTray auditTray = this.findById(auditTrayId);
        Nomenclator nomenclator = nomenclatorService.findById(nomenclatorId);
        if (auditTray.getNomenclators().contains(nomenclator))
            throw new ObjectAlreadyExistsException("auditTray.nomenclatorAlreadyExists",
                    nomenclator.getNomenclatorCode(), auditTray.getName());
        auditTray.getNomenclators().add(nomenclator);
        AuditTray updatedAuditTray = auditTrayRepository.save(auditTray);
        return this.getProjectionFactory().createProjection(AuditTrayProjection.Extended.class, updatedAuditTray);
    }

    @Override
    public AuditTrayProjection.Extended removeNomenclator(long auditTrayId, long nomenclatorId) throws ObjectNotFoundException, ObjectNotValidException {
        AuditTray auditTray = this.findById(auditTrayId);
        Nomenclator nomenclator = nomenclatorService.findById(nomenclatorId);
        if (!auditTray.getNomenclators().contains(nomenclator))
            throw new ObjectNotFoundException(
                    "auditTray.nomenclatorNotFound",
                    nomenclator.getNomenclatorCode(),
                    auditTray.getName());
        if (auditTray.getNomenclators().size() == 1)
            throw new ObjectNotValidException("auditTray.nomenclatorRequirement");
        auditTray.getNomenclators().remove(nomenclator);
        AuditTray updatedAuditTray = auditTrayRepository.save(auditTray);
        return this.getProjectionFactory().createProjection(AuditTrayProjection.Extended.class, updatedAuditTray);
    }

    @Override
    public AuditTrayProjection.Extended addAuditor(long auditTrayId, AuditorDTO auditorDTO) throws ObjectNotFoundException, ObjectAlreadyExistsException {
        AuditTray auditTray = this.findById(auditTrayId);
        Auditor mappedAuditor = this.getObjectMapper().convertValue(auditorDTO, Auditor.class);
        Auditor auditor = getOrCreateAuditor(mappedAuditor);
        if (auditTray.getAuditors().contains(auditor))
            throw new ObjectAlreadyExistsException("auditTray.auditorAlreadyExists", auditor.getUsername());
        auditTray.getAuditors().add(auditor);
        AuditTray updatedAuditTray = auditTrayRepository.save(auditTray);
        return this.getProjectionFactory().createProjection(AuditTrayProjection.Extended.class, updatedAuditTray);
    }

    @Override
    public AuditTrayProjection.Extended removeAuditor(long auditTrayId, long auditorId) throws ObjectNotFoundException, ObjectNotValidException {
        AuditTray auditTray = this.findById(auditTrayId);
        Auditor auditor = auditorService.findById(auditorId);
        if (!auditTray.getAuditors().contains(auditor))
            throw new ObjectNotFoundException("auditTray.auditorNotFound", auditor.getUsername());
        if (auditTray.getAuditors().size() == 1)
            throw new ObjectNotValidException("auditTray.auditorRequirement");
        auditTray.getAuditors().remove(auditor);
        AuditTray updatedAuditTray = auditTrayRepository.save(auditTray);
        return this.getProjectionFactory().createProjection(AuditTrayProjection.Extended.class, updatedAuditTray);
    }

    @Override
    public Optional<AuditTray> resolveCorrespondingQueueName(MedicalAuthorizationItem medicalAuthorizationItem, City city) {
        return auditTrayRepository.findByNomenclatorsAndCityOrRegion(Collections.singleton(medicalAuthorizationItem.getNomenclator()),
                Collections.singleton(city));
    }

    @Override
    public String buildAuditTrayBroadcastTopicName(String auditTrayResourceId, boolean prefix) {
        String auditTrayName = StringUtils.join(AUDIT_TRAYS, DOT, auditTrayResourceId, DOT, BROADCAST);
        String auditTrayTopicName = MQNameBuilder.buildTopicName(applicationProperties.getActiveProfile(), TenantContext.getTenant().toString(), auditTrayName);
        if (prefix)
            return auditTrayTopicName;
        return StringUtils.remove(auditTrayTopicName, StringUtils.join(SLASH, TOPIC, SLASH));
    }

    @Override
    public String buildAuditTrayOnlineQueueName(String auditTrayResourceId, boolean prefix) {
        String auditTrayName = StringUtils.join(AUDIT_TRAYS, DOT, auditTrayResourceId, DOT, ONLINE);
        String auditTrayQueueName = MQNameBuilder.buildQueueName(applicationProperties.getActiveProfile(), TenantContext.getTenant().toString(), auditTrayName);
        if (prefix)
            return auditTrayQueueName;
        return StringUtils.remove(auditTrayQueueName, StringUtils.join(SLASH, QUEUE, SLASH));
    }

    @Override
    public String buildAuditorConnectionsTopicName() {
        return MQNameBuilder.buildTopicName(applicationProperties.getActiveProfile(), TenantContext.getTenant().toString(), AUDITOR_CONNECTIONS);
    }

    @Override
    public AuditTray findByResourceId(UUID auditTrayResourceId) throws ObjectNotFoundException {
        return auditTrayRepository
                .findByResourceId(auditTrayResourceId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "auditTray.notFound",
                        auditTrayResourceId.toString()));
    }

    @Override
    public Set<AuditTray> findAllByResourceId(Collection<UUID> auditTrayResourceId) {
        return auditTrayRepository.findAllByResourceIdIn(auditTrayResourceId);
    }

    @Override
    public Integer getAuditTrayConsumers(AuditTray auditTray) {
        String auditTrayOnlineQueueName = buildAuditTrayOnlineQueueName(auditTray.getResourceId().toString(), false);
        return amqpService.getQueueActiveConsumers(auditTrayOnlineQueueName);
    }

    @Override
    public Optional<Specification<AuditTray>> appendCustomSpecification() {
        if (SecurityUtils.isAuditor())
            return Optional.of((root, query, builder) -> {
                var join = root.join("auditors");
                return builder.equal(join.get("sub"), SecurityUtils.getAuthenticatedAuthoritySub().orElse(null));
            });
        return Optional.empty();
    }
}
