package com.capacidad.validationapi.module.audittray.service;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.audittray.model.AuditHistory;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.fasterxml.jackson.databind.JsonNode;

public interface AuditTraySender {

    void audit(MedicalAuthorization medicalAuthorization) throws ObjectNotValidException;

    void sendIssueToAuditTrayQueue(AuditHistory auditHistory);

    void sendMessageToAssociatedAuditTrays(long medicalAuthorizationId, JsonNode payload);

}
