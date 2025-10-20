package com.capacidad.validationapi.module.settlement.config.security;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.settlement.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettlementChecker {

    private final SettlementService settlementService;

    @Autowired
    public SettlementChecker(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    public boolean hasAccessToSettlement(long settlementId) {
        if (SecurityUtils.isPractitioner())
            return settlementService.belongsToPractitioner(settlementId);
        if (SecurityUtils.isMedicalCenter() || SecurityUtils.isOrganization())
            return settlementService.belongsToContract(settlementId);
        return SecurityUtils.isHighRankingAuthority();
    }

}
