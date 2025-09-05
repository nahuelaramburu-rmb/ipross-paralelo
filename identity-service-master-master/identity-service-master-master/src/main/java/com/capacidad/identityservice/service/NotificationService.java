package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.utils.exception.ObjectNotValidException;

public interface NotificationService {

    void registerUserContext(ApplicationUserContext applicationUserContext) throws ObjectNotValidException;

    void unregisterUserContext(ApplicationUserContext applicationUserContext) throws ObjectNotValidException;

}
