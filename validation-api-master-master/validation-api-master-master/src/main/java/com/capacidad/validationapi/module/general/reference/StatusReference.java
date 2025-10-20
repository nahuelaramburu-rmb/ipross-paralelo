package com.capacidad.validationapi.module.general.reference;

import com.capacidad.validationapi.module.general.model.Status;

import static com.capacidad.validationapi.module.general.reference.StatusScopeReference.*;

public enum StatusReference {
    AVAILABLE(1L, GENERAL),
    DISABLED(2L, GENERAL),
    VALIDATION_PENDING(3L, VALIDATION),
    VALIDATION_APPROVED(4L, VALIDATION),
    VALIDATION_PARTIALLY_APPROVED(5L, VALIDATION),
    VALIDATION_REJECTED(6L, VALIDATION),
    VALIDATION_CANCELLED(10L, VALIDATION),
    BENEFICIARY_WITH_COVERAGE(8L, BENEFICIARY),
    BENEFICIARY_WITHOUT_COVERAGE(9L, BENEFICIARY),
    OPEN_SETTLEMENT(7L, SETTLEMENT),
    CLOSED_SETTLEMENT(11L, SETTLEMENT),
    VALIDATION_AUTHORIZED(12L, VALIDATION),
    PAYED(13L, BUDGET),
    NOT_PAYED(14L, BUDGET),
    BATCH_PENDING(15L, BATCH),
    BATCH_ACTIVE(16L, BATCH),
    BATCH_CANCELLED(17L, BATCH),
    BATCH_EXPIRED(18L, BATCH),
    PROCEDURE_REVISION(19L, PROCEDURE),
    PROCEDURE_APPROVED(20L, PROCEDURE),
    PROCEDURE_REJECTED(21L, PROCEDURE),
    PROCEDURE_EXPIRED(22L, PROCEDURE),
    PRESCRIPTION_PENDING(23L, PRESCRIPTION),
    PRESCRIPTION_APPROVED(24L, PRESCRIPTION),
    PRESCRIPTION_REJECTED(25L, PRESCRIPTION),
    PRESCRIPTION_CANCELLED(26L, PRESCRIPTION),
    PRESCRIPTION_UTILIZED(27L, PRESCRIPTION),
    PRESCRIPTION_EXPIRED(28L, PRESCRIPTION),
    PRE_MEDICAL_AUTHORIZATION_CONSUMED(29L, PRE_MEDICAL_AUTHORIZATION),
    PRE_MEDICAL_AUTHORIZATION_EXPIRED(30L, PRE_MEDICAL_AUTHORIZATION),
    PRE_MEDICAL_AUTHORIZATION_CANCELLED(31L, PRE_MEDICAL_AUTHORIZATION),
    PRE_MEDICAL_AUTHORIZATION_ACTIVE(32L, PRE_MEDICAL_AUTHORIZATION);

    private final long id;
    private final StatusScopeReference statusScopeReference;

    StatusReference(long id, StatusScopeReference statusScopeReference) {
        this.id = id;
        this.statusScopeReference = statusScopeReference;
    }

    public long getId() {
        return id;
    }

    public Status getInstance() {
        Status status = new Status();
        status.setId(id);
        return status;
    }

    public StatusScopeReference getStatusScopeReference() {
        return statusScopeReference;
    }
}
