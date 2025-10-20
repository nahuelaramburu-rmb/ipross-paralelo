package com.capacidad.validationapi.module.contract.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "contract_item_seq", allocationSize = 1)
public class ModuleContractItem extends ContractItem {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

}
