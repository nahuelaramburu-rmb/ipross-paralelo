package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractAdjustment;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@TenantFilter
public interface ContractAdjustmentRepository extends BaseContractAdjustmentRepository<ContractAdjustment, Long> {

    @Query(value = "select ca from ContractAdjustment ca " +
            "left join ca.region r " +
            "left join r.cities cts " +
            "where ca.contract = ?1 and ca.nomenclator = ?2 and " +
            "((ca.region is null and ca.city in ?3) or (ca.city is null and cts in ?3))")
    Optional<ContractAdjustment> findByContractIdAndNomenclatorIdAndRegionCitiesId(Contract contract, Nomenclator nomenclator, Set<City> city);

}
