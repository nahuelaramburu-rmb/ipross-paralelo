package com.capacidad.validationapi.module.medicalcoverage.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.insuranceplan.model.InsurancePlan;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.medicalcoverage.model.MedicalCoverage;
import com.capacidad.validationapi.module.medicalcoverage.projection.MedicalCoverageProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@TenantFilter
public interface MedicalCoverageRepository extends ExtendedJpaRepository<MedicalCoverage, Long> {

    @Query(value = "select mc from MedicalCoverage mc " +
            "left join mc.region r " +
            "left join r.cities cts " +
            "where mc.insurancePlan = ?1 and " +
            "((mc.region is null and mc.city in ?2) or (mc.city is null and cts in ?2))")
    Optional<MedicalCoverage> findByInsurancePlanIdAndCityOrRegion(InsurancePlan insurancePlan, Set<City> cityIds);

    @Query(value = "select case when count(mc) > 0 then true else false end from MedicalCoverage mc " +
            "left join mc.region r " +
            "left join r.cities cts " +
            "where mc.insurancePlan = ?1 and " +
            "((mc.region is null and mc.city in ?2) or (mc.city is null and cts in ?2))")
    boolean existsByCityOrRegion(InsurancePlan insurancePlan, Set<City> cities);

    @Query(value = "select case when count(mc) > 0 then true else false end from MedicalCoverage mc " +
            "left join mc.region r " +
            "left join r.cities cts " +
            "where mc.insurancePlan = ?1 and mc.id <> ?3 and " +
            "((mc.region is null and mc.city in ?2) or (mc.city is null and cts in ?2))")
    boolean existsByCityOrRegionAndIdIsNot(InsurancePlan insurancePlan, Set<City> cities, long id);

    Set<MedicalCoverageProjection> findAllProjectedByInsurancePlanId(Long insurancePlanId);

}
