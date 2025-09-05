package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.Login;
import com.capacidad.identityservice.model.projection.LoginViewDTO;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginRepository extends ExtendedRepository<Login, Long> {

    @Query("select new com.capacidad.identityservice.model.projection.LoginViewDTO(l.id, l.createdAt, l.principal) from Login l " +
            "where l.principal = :username and l.ipAddress = :ipAddress and l.loginEvent = 'FAILURE'")
    List<LoginViewDTO> findAllByPrincipalOrIpAddressAndFailureEvent(@Param("username") String username,
                                                                    @Param("ipAddress") String ipAddress);

}
