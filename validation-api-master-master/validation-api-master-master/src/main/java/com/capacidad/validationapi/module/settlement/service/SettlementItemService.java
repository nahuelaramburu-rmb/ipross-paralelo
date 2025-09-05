package com.capacidad.validationapi.module.settlement.service;

import com.capacidad.validationapi.module.base.dto.IdDTO;
import com.capacidad.validationapi.module.base.service.BaseService;
import com.capacidad.validationapi.module.medicalauthorization.model.MedicalAuthorization;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;

import java.util.List;
import java.util.Set;

public interface SettlementItemService extends BaseService<SettlementItem, IdDTO<Long>, Long> {

    void removeItems(MedicalAuthorization medicalAuthorization);

    List<SettlementItem> findAllBySettlementIdAndItemIds(long settlementId, Set<Long> itemIds);

    List<SettlementItem> findAllBySettlementIdAndBeneficiaryCode(long settlementId, String beneficiaryCode);

}
