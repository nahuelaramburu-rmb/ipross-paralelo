package com.capacidad.validationapi.module.medicalauthorization.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class AuditorAwareEvent extends ApplicationEvent {

    private final boolean notifyAuditors;

    public AuditorAwareEvent(Object content, boolean notifyAuditors) {
        super(content);
        this.notifyAuditors = notifyAuditors;
    }

}
