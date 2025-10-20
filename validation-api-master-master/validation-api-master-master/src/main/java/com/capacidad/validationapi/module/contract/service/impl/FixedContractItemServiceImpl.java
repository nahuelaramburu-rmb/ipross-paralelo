package com.capacidad.validationapi.module.contract.service.impl;

import com.capacidad.utils.exception.ObjectAlreadyExistsException;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.calendar.model.CalendarEventType;
import com.capacidad.validationapi.module.calendar.service.HolidayService;
import com.capacidad.validationapi.module.contract.dto.ContractItemSpecialPriceDTO;
import com.capacidad.validationapi.module.contract.dto.FixedContractItemDTO;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.model.ContractItem;
import com.capacidad.validationapi.module.contract.model.ContractItemSpecialPrice;
import com.capacidad.validationapi.module.contract.model.FixedContractItem;
import com.capacidad.validationapi.module.contract.projection.ContractItemSpecialPriceProjection;
import com.capacidad.validationapi.module.contract.repository.FixedContractItemRepository;
import com.capacidad.validationapi.module.contract.service.ContractItemSpecialPriceService;
import com.capacidad.validationapi.module.contract.service.FixedContractItemService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorizationItem;
import com.capacidad.validationapi.module.medicalcoverage.reference.ChargeTypeReference;
import com.capacidad.validationapi.module.nomenclator.model.Nomenclator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class FixedContractItemServiceImpl extends BaseContractItemServiceImpl<FixedContractItem, FixedContractItemDTO> implements FixedContractItemService {

    private final FixedContractItemRepository fixedContractItemRepository;
    private final ContractItemSpecialPriceService contractItemSpecialPriceService;
    private final HolidayService holidayService;

    @Autowired
    public FixedContractItemServiceImpl(FixedContractItemRepository repository,
                                        ContractItemSpecialPriceService contractItemSpecialPriceService,
                                        HolidayService holidayService) {
        super(repository);
        this.fixedContractItemRepository = repository;
        this.contractItemSpecialPriceService = contractItemSpecialPriceService;
        this.holidayService = holidayService;
    }

    @Override
    public FixedContractItem create(Contract contract, FixedContractItemDTO input) throws ObjectNotValidException {
        log.info("create - args: {}({})", input.getClass(), input);
        FixedContractItem contractItem = this.mapDtoToInput(input);
        contractItem.setContract(contract);
        this.validate(contractItem);
        FixedContractItem objectResponse = fixedContractItemRepository.save(contractItem);
        log.info("create - void: {}({})", contractItem.getClass(), contractItem);
        return objectResponse;
    }

    @Override
    public void validate(FixedContractItem fixedContractItem) throws ObjectNotValidException {
        if (fixedContractItem.getPractitionerCategory() != null &&
                fixedContractItemRepository
                        .existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNull
                                (fixedContractItem.getContract().getId(), fixedContractItem.getNomenclator().getId()))
            throw new ObjectAlreadyExistsException("contractItem.notCategorizedItemAlreadyExists");
        if (fixedContractItem.getPractitionerCategory() == null && fixedContractItemRepository
                .existsByContractIdAndNomenclatorIdAndPractitionerCategoryIsNotNull
                        (fixedContractItem.getContract().getId(), fixedContractItem.getNomenclator().getId()))
            throw new ObjectAlreadyExistsException("contractItem.categorizedItemAlreadyExists");
    }


    @Override
    public List<ContractItem> findContractItems(Contract contract, Nomenclator nomenclator) {
        return fixedContractItemRepository.findByContractIdAndNomenclatorId(contract.getId(), nomenclator.getId());
    }

    @Override
    public void calculateAuthorizationItemPrice(FixedContractItem contractItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        BigDecimal unitPrice = calculateUnitPriceConsideringSpecialPrice(contractItem, medicalAuthorizationItem);
        BigDecimal subtotal = unitPrice
                .multiply(new BigDecimal(medicalAuthorizationItem.getQuantity())).setScale(2, RoundingMode.HALF_UP);
        medicalAuthorizationItem.setUnitPrice(unitPrice);
        medicalAuthorizationItem.setSubtotal(subtotal);
    }

    public BigDecimal calculateUnitPriceConsideringSpecialPrice(FixedContractItem contractItem, MedicalAuthorizationItem medicalAuthorizationItem) {
        BigDecimal unitPrice = contractItem.getValue();
        Optional<CalendarEventType> eventType = holidayService.isDateHolidayOrWeekend(LocalDate.now());
        if (eventType.isPresent()) {
            Optional<ContractItemSpecialPrice> specialPrice = contractItem.getSpecialPrices().stream()
                    .filter(s -> s.getEventType().equals(eventType.get()))
                    .findAny();
            if (specialPrice.isPresent()) {
                var contractItemSpecialPrice = specialPrice.get();
                medicalAuthorizationItem.setCalendarEventType(contractItemSpecialPrice.getEventType());
                if (contractItemSpecialPrice.getChargeType().getId().equals(ChargeTypeReference.FIXED_VALUE.getId()))
                    unitPrice = contractItemSpecialPrice.getSpecialValue();
                else
                    unitPrice = unitPrice.add(contractItemSpecialPrice.getSpecialValue().multiply(contractItem.getValue())
                            .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
            }
        }
        return unitPrice;
    }

    @Override
    public ContractItemSpecialPriceProjection addContractItemSpecialPrice(long fixedContractItemId, ContractItemSpecialPriceDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        var fixedContractItem = this.findById(fixedContractItemId);
        var result = contractItemSpecialPriceService.addContractItemSpecialPrice(fixedContractItem, input);
        return this.getProjectionFactory().createProjection(ContractItemSpecialPriceProjection.class, result);
    }


}
