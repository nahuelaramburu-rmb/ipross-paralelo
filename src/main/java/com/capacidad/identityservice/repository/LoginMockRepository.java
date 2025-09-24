package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.LoginMockModel;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginMockRepository extends JpaRepository<LoginMockModel, Long> {

    // todo , trabajar sobre esto
   // Optional<LoginMockModel> buscarUsermail(String email);

    Optional<LoginMockModel> findByUsuario(String email);

}
