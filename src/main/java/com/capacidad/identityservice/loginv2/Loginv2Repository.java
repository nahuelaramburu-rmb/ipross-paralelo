package com.capacidad.identityservice.loginv2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Loginv2Repository extends JpaRepository<Loginv2Model, Long> {

    // todo , trabajar sobre esto
   // Optional<LoginMockModel> buscarUsermail(String email);

    Optional<Loginv2Model> findByUsuario(String email);

}
