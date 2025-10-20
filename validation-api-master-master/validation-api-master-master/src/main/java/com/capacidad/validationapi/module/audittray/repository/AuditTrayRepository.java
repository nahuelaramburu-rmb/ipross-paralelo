package com.capacidad.validationapi.module.audittray.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.audittray.model.AuditTray;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@TenantFilter
public interface AuditTrayRepository extends ExtendedJpaRepository<AuditTray, Long> {

    @Query(value = "select at from AuditTray at " +
            "left join at.region r " +
            "left join r.cities cts " +
            "inner join at.nomenclators as nm " +
            "where nm in ?1 and " +
            "((at.region is null and at.city in ?2) or (at.city is null and cts in ?2))")
    Optional<AuditTray> findByNomenclatorsAndCityOrRegion(Collection<Nomenclator> nomenclators, Set<City> cities);

    @Query(value = "select case when count(at) > 0 then true else false end from AuditTray at " +
            "left join at.region r " +
            "left join r.cities cts " +
            "inner join at.nomenclators as nm " +
            "where nm in ?1 and " +
            "((at.region is null and at.city in ?2) or (at.city is null and cts in ?2))")
    boolean existsByCityOrRegion(Collection<Nomenclator> nomenclators, Set<City> cities);

    @Query(value = "select case when count(at) > 0 then true else false end from AuditTray at " +
            "left join at.region r " +
            "left join r.cities cts " +
            "inner join at.nomenclators as nm " +
            "where nm in ?1 and at.id <> ?3 and " +
            "((at.region is null and at.city in ?2) or (at.city is null and cts in ?2))")
    boolean existsByCityOrRegionAndIdIsNot(Collection<Nomenclator> nomenclators, Set<City> cities, long id);

    Optional<AuditTray> findByResourceId(UUID resourceId);

    Set<AuditTray> findAllByResourceIdIn(Collection<UUID> resourceId);

}
