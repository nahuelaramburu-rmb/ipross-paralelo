package com.capacidad.validationapi.module.audittray.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditHistoryEventListener {

    private final SimpMessageSendingOperations sendingOperations;

    @Autowired
    public AuditHistoryEventListener(SimpMessageSendingOperations sendingOperations) {
        this.sendingOperations = sendingOperations;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleAuditHistoryCreation(AuditHistoryCreationEvent event) {
        sendingOperations.convertAndSend(event.getDestination(), event.getPayload());
    }

}
