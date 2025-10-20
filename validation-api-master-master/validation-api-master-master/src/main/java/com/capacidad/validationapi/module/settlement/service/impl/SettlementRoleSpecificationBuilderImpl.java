package com.capacidad.validationapi.module.settlement.service.impl;

import com.capacidad.validationapi.misc.SecurityUtils;
import com.capacidad.validationapi.module.contract.model.Contract;
import com.capacidad.validationapi.module.contract.service.ContractMediator;
import com.capacidad.validationapi.module.settlement.model.Settlement;
import com.capacidad.validationapi.module.settlement.service.SettlementRoleSpecificationBuilder;
import com.capacidad.validationapi.specification.BaseRoleSpecificationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class SettlementRoleSpecificationBuilderImpl extends BaseRoleSpecificationBuilder<Settlement, Long> implements SettlementRoleSpecificationBuilder {

    private final ContractMediator contractMediator;

    @Autowired
    public SettlementRoleSpecificationBuilderImpl(ContractMediator contractMediator) {
        this.contractMediator = contractMediator;
    }

    @Override
    public Optional<Specification<Settlement>> buildSpecificationForPractitioner() {
        return Optional.of((root, query, builder) -> {
            var join = root.join("practitioner");
            return builder.equal(join.get("resourceId"),
                    SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null));
        });
    }

    @Override
    public Optional<Specification<Settlement>> buildSpecificationForMedicalCenter() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var join = root.join("contract");
            return builder.and(join.in(contracts));
        });
    }

    @Override
    public Optional<Specification<Settlement>> buildSpecificationForOrganizations() {
        Set<Contract> contracts = contractMediator.findAllAuthContracts();
        return Optional.of((root, query, builder) -> {
            var joinContract = root.join("contract");
            var joinPractitioner = root.join("practitioner")
                    .join("medicalRegistrations")
                    .join("organization");
            return builder.and(joinContract.in(contracts), builder.equal(joinPractitioner.get("resourceId"),
                    SecurityUtils.getAuthenticatedAuthorityResourceId().orElse(null)));
        });
    }
}
