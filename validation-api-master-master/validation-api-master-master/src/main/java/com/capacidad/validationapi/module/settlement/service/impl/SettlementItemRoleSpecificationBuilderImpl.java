package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.settlement.model.SettlementItem;
import com.capacidad.validationapi.module.settlement.service.SettlementItemRoleSpecificationBuilder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class SettlementItemRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<SettlementItem, Long> implements SettlementItemRoleSpecificationBuilder {

    private final ContractMediator contractMediator;

    @Autowired
    public SettlementItemRoleSpecificationBuilderImpl(ContractMediator contractMediator) {
        this.contractMediator = contractMediator;
    }

    @Override
    public Optional<Specification<SettlementItem>> buildSpecificationForBeneficiaries() {
        return Optional.empty();
    }

    @Override
    public Optional<Specification<SettlementItem>> buildSpecificationForPractitioner() {
        return Optional.of((root, query, builder) -> {
            var mainRoot = root.join("settlement");
            var join = mainRoot.join("practitioner");
            return builder.equal(join.get("resourceId"),
                    SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<SettlementItem>> buildSpecificationForMedicalCenter() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var mainRoot = root.join("settlement");
            var join = mainRoot.join("contract");
            return builder.and(join.in(contracts));
        });
    }

    @Override
    public Optional<Specification<SettlementItem>> buildSpecificationForOrganizations() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var mainRoot = root.join("settlement");
            var joinContract = mainRoot.join("contract");
            var joinPractitioner = mainRoot.join("practitioner")
                    .join("medicalRegistrations")
                    .join("organization");
            return builder.and(joinContract.in(contracts), builder.equal(joinPractitioner.get("resourceId"),
                    SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)));
        });
    }
}
