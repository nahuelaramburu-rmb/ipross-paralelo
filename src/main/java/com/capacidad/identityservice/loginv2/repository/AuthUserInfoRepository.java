package com.capacidad.identityservice.loginv2.repository;

import com.capacidad.identityservice.loginv2.model.AuthUserInfo;
import com.capacidad.identityservice.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserInfoRepository extends JpaRepository< AuthUserInfo,Long > {

    Optional<AuthUserInfo> findByNombre(String nombre);
}
