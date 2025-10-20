package com.capacidad.validationapi.module.storage.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
@Setter
public class NewAttachmentEvent extends ApplicationEvent {

    private final long relatedId;
    private final String filename;
    private final UUID tenantId;

    public NewAttachmentEvent(long relatedId, String filename, UUID tenantId) {
        super(relatedId);
        this.relatedId = relatedId;
        this.filename = filename;
        this.tenantId = tenantId;
    }

}
