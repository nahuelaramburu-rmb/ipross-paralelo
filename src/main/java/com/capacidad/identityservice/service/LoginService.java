package com.capacidad.identityservice.service;

import com.capacidad.identityservice.model.Login;
import com.capacidad.identityservice.model.LoginEvent;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface LoginService {

    void registerLoginAttempt(Object principal, LoginEvent event, HttpServletRequest request);

    void resetLoginAttempts(List<Login> loginList);

}
