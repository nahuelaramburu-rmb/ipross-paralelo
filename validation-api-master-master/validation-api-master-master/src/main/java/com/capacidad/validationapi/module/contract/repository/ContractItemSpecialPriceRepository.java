package com.capacidad.validationapi.module.contract.repository;

import com.capacidad.validationapi.module.base.repository.ExtendedJpaRepository;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.contract.model.ContractItemSpecialPrice;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractItemSpecialPriceRepository extends ExtendedJpaRepository<ContractItemSpecialPrice, Long> {

    Optional<ContractItemSpecialPrice> findByContractItemIdAndEventType(long contractItemId, CalendarEventType eventType);

}
