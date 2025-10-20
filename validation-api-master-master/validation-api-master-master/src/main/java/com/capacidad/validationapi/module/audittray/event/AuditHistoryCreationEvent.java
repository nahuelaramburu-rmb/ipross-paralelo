package com.capacidad.validationapi.module.audittray.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class AuditHistoryCreationEvent extends ApplicationEvent {

    private String destination;
    private transient Object payload;

    public AuditHistoryCreationEvent(String destination, Object payload) {
        super(payload);
        this.destination = destination;
        this.payload = payload;
    }
}
