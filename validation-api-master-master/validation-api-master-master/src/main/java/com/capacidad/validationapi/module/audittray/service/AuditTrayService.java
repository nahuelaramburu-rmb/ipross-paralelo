package com.capacidad.validationapi.module.audittray.service;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.dto.AuditTrayDTO;
import com.capacidad.validationapi.module.audittray.dto.AuditorDTO;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.audittray.projection.AuditTrayProjection;
import com.capacidad.validationapi.module.audittray.projection.AuditorProjection;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.nomenclator.projection.NomenclatorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AuditTrayService extends BaseService<AuditTray, AuditTrayDTO, Long> {

    Page<NomenclatorProjection.Minor> getAuditTrayNomenclators(long auditTrayId, Pageable pageable);

    Page<AuditorProjection> getAuditTrayAuditors(long auditTrayId, Pageable pageable);

    AuditTrayProjection.Extended addNomenclator(long auditTrayId, long nomenclatorId) throws ObjectNotFoundException, ObjectAlreadyExistsException;

    AuditTrayProjection.Extended removeNomenclator(long auditTrayId, long nomenclatorId) throws ObjectNotFoundException, ObjectNotValidException;

    AuditTrayProjection.Extended addAuditor(long auditTrayId, AuditorDTO auditorDTO) throws ObjectNotFoundException, ObjectAlreadyExistsException;

    AuditTrayProjection.Extended removeAuditor(long auditTrayId, long auditorId) throws ObjectNotFoundException, ObjectNotValidException;

    Optional<AuditTray> resolveCorrespondingQueueName(MedicalAuthorizationItem medicalAuthorizationItem, City city);

    String buildAuditTrayOnlineQueueName(String auditTrayResourceId, boolean prefix);

    String buildAuditTrayBroadcastTopicName(String auditTrayResourceId, boolean prefix);

    String buildAuditorConnectionsTopicName();

    AuditTray findByResourceId(UUID auditTrayResourceId) throws ObjectNotFoundException;

    Set<AuditTray> findAllByResourceId(Collection<UUID> auditTrayResourceId);

    Integer getAuditTrayConsumers(AuditTray auditTray);

}
