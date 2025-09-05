package com.capacidad.validationapi.module.procedure.event;

import com.capacidad.validationapi.module.procedure.model.Procedure;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ProcedureResolutionEvent extends ApplicationEvent {

    private final Procedure procedure;

    public ProcedureResolutionEvent(Procedure procedure) {
        super(procedure);
        this.procedure = procedure;
    }

}
