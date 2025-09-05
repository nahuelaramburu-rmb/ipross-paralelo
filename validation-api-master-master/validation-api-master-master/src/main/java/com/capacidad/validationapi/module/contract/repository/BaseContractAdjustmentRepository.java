package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Set;

@NoRepositoryBean
public interface BaseContractAdjustmentRepository<T extends ContractAdjustment, I extends Serializable> extends ExtendedJpaRepository<T, I> {

    @Query(value = "select case when count(ca)> 0 then true else false end from ContractAdjustment ca " +
            "left join ca.region r " +
            "left join r.cities cts " +
            "where ca.contract= ?2 and ca.nomenclator = ?1 and " +
            "((ca.region is null and ca.city in ?3) or (ca.city is null and cts in ?3))")
    boolean existsByRegionOrCity(Nomenclator nomenclator, Contract contract, Set<City> cities);

    @Query(value = "select case when count(ca)> 0 then true else false end from ContractAdjustment ca " +
            "left join ca.region r " +
            "left join r.cities cts " +
            "where ca.contract= ?2 and ca.nomenclator = ?1 and ca.id <> ?4 and " +
            "((ca.region is null and ca.city in ?3) or (ca.city is null and cts in ?3))")
    boolean existsByRegionOrCityAndIdIsNot(Nomenclator nomenclator, Contract contract, Set<City> cities, long id);

}
