package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.Role;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends ExtendedRepository<Role, Long> {

    Optional<Role> findByNameIgnoreCase(String name);

    List<Role> findAllByNameIn(List<String> names);

    Optional<Role> findByName(String name);
}
