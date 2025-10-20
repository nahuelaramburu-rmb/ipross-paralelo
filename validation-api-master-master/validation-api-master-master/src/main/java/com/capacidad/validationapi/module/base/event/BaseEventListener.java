package com.capacidad.validationapi.module.base.event;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.Serializable;

@Log4j2
@Component
public class BaseEventListener {

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public <T, I extends Serializable> void handleAfterSoftDeleteEvent(AfterSoftDeleteEvent<T, I> sofDeleteEvent) {
        try {
            sofDeleteEvent.getRepository().delete(sofDeleteEvent.getSource());
            sofDeleteEvent.getRepository().flush();
        } catch (DataIntegrityViolationException e) {
            log.warn("{} - handleAfterSoftDeleteEvent: DataIntegrityViolationException {} ", this.getClass(), e.getMessage());
        }
    }

}
