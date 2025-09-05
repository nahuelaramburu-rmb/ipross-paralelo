package com.capacidad.validationapi.module.procedure.event;

import com.capacidad.validationapi.module.procedure.model.Procedure;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class ProcedureNewMessageEvent extends ApplicationEvent {

    private final Procedure procedure;
    private final List<String> roles;

    public ProcedureNewMessageEvent(Procedure procedure, List<String> roles) {
        super(procedure);
        this.procedure = procedure;
        this.roles = roles;
    }

}
