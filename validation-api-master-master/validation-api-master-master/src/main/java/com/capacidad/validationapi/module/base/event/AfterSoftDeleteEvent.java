package com.capacidad.validationapi.module.base.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

@Getter
public class AfterSoftDeleteEvent<T, I extends Serializable> extends ApplicationEvent {

    private final T source;
    private final JpaRepository<T, I> repository;

    public AfterSoftDeleteEvent(T source, JpaRepository<T, I> repository) {
        super(source);
        this.source = source;
        this.repository = repository;
    }

}
