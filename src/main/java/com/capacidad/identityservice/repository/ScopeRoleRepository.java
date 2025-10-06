package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.Operation;
import com.capacidad.identityservice.model.ScopeRole;
import com.capacidad.identityservice.model.projection.ScopeRoleViewDTO;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ScopeRoleRepository extends ExtendedRepository<ScopeRole, Long> {

    @Query(value = "select new com.capacidad.identityservice.model.projection.ScopeRoleViewDTO(re.name, sr.operations) " +
            "from ScopeRole sr " +
            "inner join sr.resource as re " +
            "inner join sr.tenant as t " +
            "inner join sr.role as r " +
            "where r.name = ?1 and t.tenantId = ?2")
    Set<ScopeRoleViewDTO> findAllByRoleNameAndTenantTenantId(String roleName, UUID tenantId);

    @Query(value = "select new com.capacidad.identityservice.model.projection.ScopeRoleViewDTO(re.name, sr.operations) " +
            "from ScopeRole sr " +
            "inner join sr.resource as re " +
            "inner join sr.role as r " +
            "where r.name = ?1 and sr.tenant is null")
    Set<ScopeRoleViewDTO> findAllByRoleNameAndTenantIsNull(String roleName);


    // obtiene las operaciones de cada rol
    @Query("select sr.operations from ScopeRole sr " +
            "inner join sr.role r " +
            "where r.name = :roleName")
    List<Operation> findAllOperationsByRoleName(String roleName);



}
