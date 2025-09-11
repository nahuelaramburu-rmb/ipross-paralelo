package com.capacidad.identityservice.model.projection;

import com.capacidad.identityservice.model.Login;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LoginViewDTO {

    private final Long id;
    private final LocalDateTime createdAt;
    private final String principal;

    public LoginViewDTO(Long id, LocalDateTime createdAt, String principal) {
        this.id = id;
        this.createdAt = createdAt;
        this.principal = principal;
    }

    public Login buildLogin() {
        Login login = new Login();
        login.setId(getId());
        login.setPrincipal(getPrincipal());
        return login;
    }

}
