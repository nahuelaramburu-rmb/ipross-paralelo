package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.config.multitenancy.TenantFilter;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@TenantFilter
public interface FixedContractItemRepository extends BaseContractItemRepository<FixedContractItem, Long> {

    List<ContractItem> findByContractIdAndNomenclatorId(Long contractId, Long nomenclatorId);

    boolean existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNull(Long contractId, Long nomenclatorId);

    boolean existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNotNull(Long contractId, Long nomenclatorId);

}
